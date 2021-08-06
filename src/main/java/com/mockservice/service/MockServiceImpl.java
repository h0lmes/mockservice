package com.mockservice.service;

import com.mockservice.domain.Route;
import com.mockservice.domain.RouteType;
import com.mockservice.repository.ConfigRepository;
import com.mockservice.request.RequestFacade;
import com.mockservice.response.MockResponse;
import com.mockservice.response.RestMockResponse;
import com.mockservice.response.SoapMockResponse;
import com.mockservice.template.TemplateEngine;
import com.mockservice.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ConcurrentLruCache;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class MockServiceImpl implements MockService {

    private static final Logger log = LoggerFactory.getLogger(MockServiceImpl.class);

    private final TemplateEngine templateEngine;
    private final RouteService routeService;
    private final ActiveScenariosService activeScenariosService;
    private final ConfigRepository configRepository;
    private final QuantumTheory quantumTheory;
    private final ConcurrentLruCache<Route, MockResponse> responseCache;
    private final RequestService requestService;

    public MockServiceImpl(@Value("${application.cache.response}") int cacheSize,
                           TemplateEngine templateEngine,
                           RouteService routeService,
                           ActiveScenariosService activeScenariosService,
                           ConfigRepository configRepository,
                           QuantumTheory quantumTheory,
                           RequestService requestService) {
        this.templateEngine = templateEngine;
        this.routeService = routeService;
        this.activeScenariosService = activeScenariosService;
        this.configRepository = configRepository;
        this.quantumTheory = quantumTheory;
        this.requestService = requestService;
        responseCache = new ConcurrentLruCache<>(cacheSize, this::routeToMockResponse);
    }

    private MockResponse routeToMockResponse(Route route) {
        if (RouteType.REST.equals(route.getType())) {
            return new RestMockResponse(route.getResponse());
        } else {
            return new SoapMockResponse(route.getResponse());
        }
    }

    @Override
    public void cacheRemove(Route route) {
        if (responseCache.remove(route)) {
            log.info("Route evicted: {}", route);
        }
    }

    @Override
    public ResponseEntity<String> mock(RequestFacade request) {
        Route route = getRoute(request);

        var validationResult = validateRequestBody(route, request.getBody());
        route = validationResult.getRoute();

        MockResponse response = responseCache.get(route);
        Map<String, String> vars = request.getVariables();
        if (!validationResult.isOk()) vars.putAll(validationResult.getVariables());
        response.setVariables(vars, templateEngine.getFunctions());
        int statusCode = route.getResponseCode();
        String body = response.getResponseBody();

        if (route.isRest() && configRepository.getSettings().getQuantum()) {
            body = quantumTheory.apply(body);
            statusCode = quantumTheory.apply(statusCode);
            quantumTheory.delay();
        }

        response.ifHasRequest(requestService::schedule);

        return ResponseEntity
                .status(statusCode)
                .headers(response.getResponseHeaders())
                .body(body);
    }

    private Route getRoute(RequestFacade request) {
        String alt = request.getAlt()
                .or(() -> activeScenariosService.getAltFor(request.getRequestMethod(), request.getEndpoint()))
                .or(() -> maybeGetRandomAltFor(request.getRequestMethod(), request.getEndpoint()))
                .orElse("");

        Route searchRoute = new Route()
                .setMethod(request.getRequestMethod())
                .setPath(request.getEndpoint())
                .setAlt(alt);

        log.info("Route requested: {}", searchRoute);

        return routeService
                .getEnabledRoute(searchRoute)
                .orElseThrow(() -> new NoRouteFoundException(searchRoute));
    }

    private Optional<? extends String> maybeGetRandomAltFor(RequestMethod method, String path) {
        if (configRepository.getSettings().getRandomAlt() || configRepository.getSettings().getQuantum()) {
            return routeService.getRandomAltFor(method, path);
        }
        return Optional.empty();
    }

    private RequestBodyValidationResult validateRequestBody(Route route, String body) {
        String schema = route.getRequestBodySchema();
        try {
            if (!schema.isEmpty()) JsonUtils.validate(body, schema);
        } catch (RuntimeException e) {
            return returnValidationErrorOrThrow(e, route);
        }
        return RequestBodyValidationResult.success(route);
    }

    private RequestBodyValidationResult returnValidationErrorOrThrow(RuntimeException e, Route route) {
        if (configRepository.getSettings().getAlt400OnFailedRequestValidation()) {
            Route route400 = getRoute400For(route);
            if (route400 != null) {
                log.info("Route requested: {}", route400);
                Map<String, String> vars = new HashMap<>();
                vars.put("requestBodyValidationErrorMessage", e.toString());
                return RequestBodyValidationResult.error(e, route400, vars);
            }
        }

        throw e;
    }

    private Route getRoute400For(Route route) {
        Route route400 = new Route(route).setAlt("400");
        return routeService.getEnabledRoute(route400).orElse(null);
    }
}
