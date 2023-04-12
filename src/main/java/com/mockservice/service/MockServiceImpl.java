package com.mockservice.service;

import com.mockservice.domain.Route;
import com.mockservice.domain.RouteType;
import com.mockservice.repository.ConfigRepository;
import com.mockservice.request.RequestFacade;
import com.mockservice.response.MockResponse;
import com.mockservice.response.RestMockResponse;
import com.mockservice.response.SoapMockResponse;
import com.mockservice.service.route.RouteService;
import com.mockservice.template.MockVariables;
import com.mockservice.template.TemplateEngine;
import com.mockservice.validate.DataValidationException;
import com.mockservice.validate.DataValidator;
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
    private final ScenarioService scenarioService;
    private final ConfigRepository configRepository;
    private final RequestService requestService;
    private final List<QuantumTheory> quantumTheories;
    private final List<DataValidator> dataValidators;
    private final ConcurrentLruCache<Route, MockResponse> responseCache;

    public MockServiceImpl(@Value("${application.mock-service.cache-size}") int cacheSize,
                           TemplateEngine templateEngine,
                           RouteService routeService,
                           ScenarioService scenarioService,
                           ConfigRepository configRepository,
                           RequestService requestService,
                           List<QuantumTheory> quantumTheories,
                           List<DataValidator> dataValidators) {
        this.templateEngine = templateEngine;
        this.routeService = routeService;
        this.scenarioService = scenarioService;
        this.configRepository = configRepository;
        this.requestService = requestService;
        this.quantumTheories = quantumTheories;
        this.dataValidators = dataValidators;
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

        MockVariables variables = request.getVariables(Optional.ofNullable(routeService.getRouteVariables(route)));
        response.setVariables(variables, templateEngine.getFunctions());
        validationResult.ifError(response::addVariables);

        ResponseEntity<String> responseEntity = responseEntityFromResponse(response);
        responseEntity = maybeApplyQuantumTheory(responseEntity);

        response.ifHasRequest(requestService::schedule);

        return responseEntity;
    }

    private Route findRouteForRequest(RequestFacade request) {
        Optional<Route> maybeRoute = routeService.getRouteForVariables(
                request.getRequestMethod(),
                request.getEndpoint(),
                request.getVariables(Optional.empty()));

        if (maybeRoute.isPresent()) {
            log.info("Route requested (defined by variables): {}", maybeRoute.get());
            return maybeRoute.get();
        }

        String alt = request.getAlt()
                .or(() -> scenarioService.getAltFor(request.getRequestMethod(), request.getEndpoint()))
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

    private Optional<String> maybeGetRandomAltFor(RequestMethod method, String path) {
        if (configRepository.getSettings().getRandomAlt() || configRepository.getSettings().getQuantum()) {
            return routeService.getRandomAltFor(method, path);
        }
        return Optional.empty();
    }

    private RequestBodyValidationResult validateRequestBody(Route route, String body) {
        String schema = route.getRequestBodySchema();
        try {
            if (!schema.isEmpty()) {
                for (DataValidator validator : dataValidators) {
                    if (validator.applicable(body)) {
                        validator.validate(body, schema);
                    }
                }
            }
        } catch (DataValidationException e) {
            return handleValidationException(e, route);
        }
        return RequestBodyValidationResult.success(route);
    }

    private RequestBodyValidationResult handleValidationException(DataValidationException e, Route route) {
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

    private ResponseEntity<String> maybeApplyQuantumTheory(ResponseEntity<String> responseEntity) {
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
