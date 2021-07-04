package com.mockservice.service;

import com.mockservice.domain.Route;
import com.mockservice.domain.RouteType;
import com.mockservice.quantum.QuantumTheory;
import com.mockservice.quantum.RandomUtil;
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

    public MockServiceImpl(@Value("${application.cache.response}") int cacheSizeLimit,
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
        responseCache = new ConcurrentLruCache<>(cacheSizeLimit, this::getResponse);
        this.requestService = requestService;
    }

    private MockResponse getResponse(Route route) {
        return routeService.getEnabledRoute(route)
                .map(this::routeToMockResponse)
                .orElse(null);
    }

    private MockResponse routeToMockResponse(Route route) {
        log.info("Route requested: {}", route);
        if (RouteType.REST.equals(route.getType())) {
            return new RestMockResponse(templateEngine, route.getResponse());
        }
        return new SoapMockResponse(templateEngine, route.getResponse());
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
        MockResponse response = responseCache.get(route);
        response.setVariables(request.getVariables());
        String body = response.getResponseBody();
        int statusCode = response.getResponseCode();

        if (configRepository.getSettings().getQuantum()) {
            if (RouteType.REST.equals(route.getType())) {
                body = quantumTheory.apply(body);
            }

            delay();
            if (RandomUtil.withChance(20)) {
                statusCode = quantumTheory.randomStatusCode();
            }
        }

        if (response.hasRequest()) {
            requestService.schedule(response);
        }

        return ResponseEntity
                .status(statusCode)
                .headers(response.getResponseHeaders())
                .body(body);
    }

    private Route getRoute(RequestFacade requestFacade) {
        Route route = new Route()
                .setMethod(requestFacade.getRequestMethod())
                .setPath(requestFacade.getEndpoint());

        boolean random = configRepository.getSettings().getRandomAlt() || configRepository.getSettings().getQuantum();
        String alt = requestFacade.getAlt()
                .or(() -> random ? routeService.getRandomAltFor(route) : Optional.empty())
                .or(() -> activeScenariosService.getAltFor(requestFacade.getRequestMethod(), requestFacade.getEndpoint()))
                .orElse("");
        return route.setAlt(alt);
    }
}
