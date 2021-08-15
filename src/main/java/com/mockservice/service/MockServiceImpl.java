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
import com.mockservice.util.JsonValidationException;
import com.mockservice.service.quantum.QuantumTheory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ConcurrentLruCache;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Optional;

@Service
public class MockServiceImpl implements MockService {

    private static final Logger log = LoggerFactory.getLogger(MockServiceImpl.class);

    private final TemplateEngine templateEngine;
    private final RouteService routeService;
    private final ActiveScenariosService activeScenariosService;
    private final ConfigRepository configRepository;
    private final RequestService requestService;
    private final List<QuantumTheory> quantumTheories;
    private final ConcurrentLruCache<Route, MockResponse> responseCache;

    public MockServiceImpl(@Value("${application.cache.response}") int cacheSize,
                           TemplateEngine templateEngine,
                           RouteService routeService,
                           ActiveScenariosService activeScenariosService,
                           ConfigRepository configRepository,
                           RequestService requestService,
                           List<QuantumTheory> quantumTheories) {
        this.templateEngine = templateEngine;
        this.routeService = routeService;
        this.activeScenariosService = activeScenariosService;
        this.configRepository = configRepository;
        this.requestService = requestService;
        this.quantumTheories = quantumTheories;
        responseCache = new ConcurrentLruCache<>(cacheSize, this::mockResponseFromRoute);
    }

    private MockResponse mockResponseFromRoute(Route route) {
        if (RouteType.REST.equals(route.getType())) {
            return new RestMockResponse(route.getResponseCode(), route.getResponse());
        } else {
            return new SoapMockResponse(route.getResponseCode(), route.getResponse());
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
        Route route = findRouteForRequest(request);

        var validationResult = validateRequestBody(route, request.getBody());
        route = validationResult.getRoute();

        MockResponse response = responseCache.get(route);

        response.setVariables(request.getVariables(), templateEngine.getFunctions());
        validationResult.ifError(response::addVariables);

        ResponseEntity<String> responseEntity = responseEntityFromResponse(response);
        responseEntity = applyQuantumTheoryIfSetSo(responseEntity);

        response.ifHasRequest(requestService::schedule);

        return responseEntity;
    }

    private Route findRouteForRequest(RequestFacade request) {
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
            if (!schema.isEmpty())
                JsonUtils.validate(body, schema);
        } catch (JsonValidationException e) {
            return validationErrorOrThrow(e, route);
        }
        return RequestBodyValidationResult.success(route);
    }

    private RequestBodyValidationResult validationErrorOrThrow(JsonValidationException e, Route route) {
        if (configRepository.getSettings().getAlt400OnFailedRequestValidation()) {
            Route route400 = getRoute400For(route);
            if (route400 != null) {
                log.info("Validation error. Route 400: {}", route400);
                return RequestBodyValidationResult.error(route400, e);
            }
        }

        throw e;
    }

    private Route getRoute400For(Route route) {
        Route route400 = new Route(route).setAlt("400");
        return routeService.getEnabledRoute(route400).orElse(null);
    }

    private ResponseEntity<String> responseEntityFromResponse(MockResponse response) {
        return ResponseEntity
                .status(response.getResponseCode())
                .headers(response.getResponseHeaders())
                .body(response.getResponseBody());
    }

    private ResponseEntity<String> applyQuantumTheoryIfSetSo(ResponseEntity<String> responseEntity) {
        if (configRepository.getSettings().getQuantum()) {
            String body = responseEntity.getBody();
            for (QuantumTheory theory : quantumTheories) {
                if (theory.applicable(body)) {
                    body = theory.apply(body);
                    int statusCode = theory.apply(responseEntity.getStatusCodeValue());
                    theory.delay();
                    return ResponseEntity
                            .status(statusCode)
                            .headers(responseEntity.getHeaders())
                            .body(body);
                }
            }
        }
        return responseEntity;
    }
}
