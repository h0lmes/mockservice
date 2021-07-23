package com.mockservice.service;

import com.mockservice.domain.Route;
import com.mockservice.domain.RouteType;
import com.mockservice.repository.ConfigRepository;
import com.mockservice.request.RequestFacade;
import com.mockservice.response.MockResponse;
import com.mockservice.response.RestMockResponse;
import com.mockservice.response.SoapMockResponse;
import com.mockservice.template.TemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ConcurrentLruCache;

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
            return new RestMockResponse(templateEngine, route.getResponse());
        } else {
            return new SoapMockResponse(templateEngine, route.getResponse());
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
        log.info("Route requested: {}", route);
        int statusCode = route.getResponseCode();

        MockResponse response = responseCache.get(route);
        response.setVariables(request.getVariables());
        String body = response.getResponseBody();

        if (configRepository.getSettings().getQuantum() && RouteType.REST.equals(route.getType())) {
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
        return routeService
                .getEnabledRoute(
                        new Route()
                                .setMethod(request.getRequestMethod())
                                .setPath(request.getEndpoint())
                                .setAlt(getRouteAlt(request))
                )
                .orElseThrow(NoRouteFoundException::new);
    }

    private String getRouteAlt(RequestFacade request) {
        return request.getAlt()
                .or(() -> activeScenariosService.getAltFor(request.getRequestMethod(), request.getEndpoint()))
                .or(() -> {
                    if (configRepository.getSettings().getRandomAlt() || configRepository.getSettings().getQuantum()) {
                        return routeService.getRandomAltFor(request.getRequestMethod(), request.getEndpoint());
                    } else {
                        return Optional.empty();
                    }
                })
                .orElse("");
    }
}
