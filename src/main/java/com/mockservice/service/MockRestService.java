package com.mockservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockservice.domain.Route;
import com.mockservice.quantum.QuantumTheory;
import com.mockservice.quantum.RandomUtil;
import com.mockservice.request.RequestFacade;
import com.mockservice.response.MockResponse;
import com.mockservice.response.RestMockResponse;
import com.mockservice.template.TemplateEngine;
import com.mockservice.web.webapp.ErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ConcurrentLruCache;

import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;

@Service("rest")
public class MockRestService implements MockService {

    private static final Logger log = LoggerFactory.getLogger(MockRestService.class);

    private final TemplateEngine templateEngine;
    private final RouteService routeService;
    private final ActiveScenariosService activeScenariosService;
    private final ConfigRepository configRepository;
    private final QuantumTheory quantumTheory;
    private final ConcurrentLruCache<Route, MockResponse> resourceCache;
    private final LinkedBlockingQueue<MockResponse> requestQueue = new LinkedBlockingQueue<>();

    public MockRestService(@Value("${application.cache.rest-resource}") int cacheSizeLimit,
                           TemplateEngine templateEngine,
                           RouteService routeService,
                           ActiveScenariosService activeScenariosService,
                           ConfigRepository configRepository,
                           QuantumTheory quantumTheory) {
        this.templateEngine = templateEngine;
        this.routeService = routeService;
        this.activeScenariosService = activeScenariosService;
        this.configRepository = configRepository;
        this.quantumTheory = quantumTheory;
        resourceCache = new ConcurrentLruCache<>(cacheSizeLimit, this::loadResource);
    }

    private MockResponse loadResource(Route route) {
        return routeService.getEnabledRoute(route)
                .map(r -> new RestMockResponse(templateEngine, r.getResponse()))
                .orElse(null);
    }

    @Override
    public void cacheRemove(Route route) {
        if (resourceCache.remove(route)) {
            log.info("Route evicted: {}", route);
        }
    }

    @Override
    public ResponseEntity<String> mock(RequestFacade request) {
        Route route = getRoute(request);
        log.info("Route requested: {}", route);
        MockResponse resource = resourceCache.get(route);
        resource.setVariables(request.getVariables()).setHost(request.getBasePath());
        String body = resource.getResponseBody();
        int statusCode = resource.getResponseCode();

        if (configRepository.getSettings().getQuantum()) {
            body = quantumTheory.apply(body);
            delay();
            if (RandomUtil.withChance(20)) {
                statusCode = quantumTheory.randomStatusCode();
            }
        }

//        try {
//            if (resource.hasRequest()) {
//                requestQueue.put(resource);
//            }
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }

        return ResponseEntity
                .status(statusCode)
                .headers(resource.getResponseHeaders())
                .body(body);
    }

    private Route getRoute(RequestFacade requestFacade) {
        Route route = new Route()
                .setMethod(requestFacade.getRequestMethod())
                .setPath(requestFacade.getEndpoint());

        boolean random = configRepository.getSettings().getRandomAlt() || configRepository.getSettings().getQuantum();
        String alt = requestFacade
                .getAlt()
                .or(() -> random ? routeService.getRandomAltFor(route) : Optional.empty())
                .or(() -> activeScenariosService.getAltFor(requestFacade.getRequestMethod(), requestFacade.getEndpoint()))
                .orElse("");
        return route.setAlt(alt);
    }

    @Override
    public String mockError(Throwable t) {
        try {
            return new ObjectMapper().writeValueAsString(new ErrorInfo(t));
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}
