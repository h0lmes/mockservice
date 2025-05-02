package com.mockachu.service;

import com.mockachu.components.QuantumTheory;
import com.mockachu.domain.Route;
import com.mockachu.domain.RouteType;
import com.mockachu.exception.RouteNotFoundException;
import com.mockachu.repository.ConfigRepository;
import com.mockachu.response.MockResponse;
import com.mockachu.response.RestMockResponse;
import com.mockachu.response.SoapMockResponse;
import com.mockachu.template.MockFunctions;
import com.mockachu.template.MockVariables;
import com.mockachu.validate.DataValidationException;
import com.mockachu.validate.DataValidator;
import com.mockachu.validate.RequestBodyValidationResult;
import com.mockachu.web.mock.MockRequestFacade;
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

    private final RouteService routeService;
    private final ScenarioService scenarioService;
    private final ConfigRepository configRepository;
    private final RequestService requestService;
    private final ContextService contextService;
    private final List<QuantumTheory> quantumTheories;
    private final List<DataValidator> dataValidators;
    private final ConcurrentLruCache<Route, MockResponse> responseCache;

    public MockServiceImpl(@Value("${application.mock-service.cache-size:1000}") int cacheSize,
                           RouteService routeService,
                           ScenarioService scenarioService,
                           ConfigRepository configRepository,
                           RequestService requestService,
                           ContextService contextService, List<QuantumTheory> quantumTheories,
                           List<DataValidator> dataValidators) {
        this.routeService = routeService;
        this.scenarioService = scenarioService;
        this.configRepository = configRepository;
        this.requestService = requestService;
        this.contextService = contextService;
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
    public ResponseEntity<String> mock(MockRequestFacade request) {
        boolean isXmlBody = routeService
                .getEnabledRouteType(request.getMethod(), request.getEndpoint())
                .orElse(RouteType.REST)
                .equals(RouteType.SOAP);

        MockVariables baseVariables = null;
        if (configRepository.getSettings() != null
                && configRepository.getSettings().isUseContextInRouteResponse()) {
            baseVariables = contextService.get();
        }
        var variables = request.getVariables(baseVariables, isXmlBody);

        var route = findRouteForRequest(request, variables);
        var validationResult = validateRequestBody(route, request.getBody());
        route = validationResult.getRoute();

        var response = responseCache.get(route);

        response.setVariables(variables, MockFunctions.create());
        validationResult.ifError(response::addVariables);

        var responseEntity = response.asResponseEntity();
        responseEntity = maybeApplyQuantumTheory(responseEntity);

        if (route.isTriggerRequest()) {
            requestService.schedule(route.getTriggerRequestIds(), route.getTriggerRequestDelay(), variables);
        }

        return responseEntity;
    }

    private Route findRouteForRequest(MockRequestFacade request, MockVariables variables) {
        var alt = request.getAlt()
                .or(() -> getAltIfQuantum(request.getMethod(), request.getEndpoint()))
                .or(() -> scenarioService.getAltFor(request.getMethod(), request.getEndpoint()))
                .or(() -> routeService.getAltFor(request.getMethod(), request.getEndpoint(), variables))
                .or(() -> getAltIfRandom(request.getMethod(), request.getEndpoint()))
                .orElse("");

        var search = new Route().setMethod(request.getMethod()).setPath(request.getEndpoint()).setAlt(alt);
        log.info("Route requested: {}", search);
        return routeService.getEnabledRoute(search)
                .orElseThrow(() -> new RouteNotFoundException(search));
    }

    private Optional<String> getAltIfQuantum(RequestMethod method, String path) {
        if (!configRepository.getSettings().isQuantum()) {
            return Optional.empty();
        }
        return routeService.getRandomAltFor(method, path);
    }

    private Optional<String> getAltIfRandom(RequestMethod method, String path) {
        if (!configRepository.getSettings().isRandomAlt()) {
            return Optional.empty();
        }
        return routeService.getRandomAltFor(method, path);
    }

    private RequestBodyValidationResult validateRequestBody(Route route, String body) {
        String schema = route.getRequestBodySchema();
        if (schema == null || schema.isEmpty()) return RequestBodyValidationResult.success(route);

        try {
            for (DataValidator validator : dataValidators) {
                if (validator.applicable(body)) {
                    validator.validate(body, schema);
                }
            }
        } catch (DataValidationException e) {
            return handleValidationException(e, route);
        }
        return RequestBodyValidationResult.success(route);
    }

    private RequestBodyValidationResult handleValidationException(DataValidationException e, Route route) {
        if (!configRepository.getSettings().isAlt400OnFailedRequestValidation()) throw e;

        var route400 = getRoute400For(route);
        if (route400 == null) throw e;

        log.info("Validation error. Route 400: {}", route400);
        return RequestBodyValidationResult.error(route400, e);
    }

    private Route getRoute400For(Route route) {
        Route route400 = new Route(route).setAlt("400");
        return routeService.getEnabledRoute(route400).orElse(null);
    }

    private ResponseEntity<String> maybeApplyQuantumTheory(ResponseEntity<String> responseEntity) {
        if (!configRepository.getSettings().isQuantum()) return responseEntity;

        for (QuantumTheory theory : quantumTheories) {
            if (theory.applicable(responseEntity.getBody())) {
                theory.delay();
                return ResponseEntity
                        .status(theory.apply(responseEntity.getStatusCode().value()))
                        .headers(responseEntity.getHeaders())
                        .body(theory.apply(responseEntity.getBody()));
            }
        }
        return responseEntity;
    }
}
