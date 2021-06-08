package com.mockservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockservice.domain.Route;
import com.mockservice.request.RequestFacade;
import com.mockservice.request.RestRequestFacade;
import com.mockservice.resource.MockResource;
import com.mockservice.resource.RestMockResource;
import com.mockservice.template.TemplateEngine;
import com.mockservice.web.webapp.ErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ConcurrentLruCache;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

@Service("rest")
public class MockRestService implements MockService {

    private static final Logger log = LoggerFactory.getLogger(MockRestService.class);

    private final HttpServletRequest request;
    private final TemplateEngine templateEngine;
    private final RouteService routeService;
    private final ActiveScenariosService activeScenariosService;
    private final ConfigRepository configRepository;
    private final QuantumTheory quantumTheory;
    private final ConcurrentLruCache<Route, MockResource> resourceCache;

    public MockRestService(@Value("${application.cache.rest-resource}") int cacheSizeLimit,
                           HttpServletRequest request,
                           TemplateEngine templateEngine,
                           RouteService routeService,
                           ActiveScenariosService activeScenariosService,
                           ConfigRepository configRepository,
                           QuantumTheory quantumTheory) {
        this.request = request;
        this.templateEngine = templateEngine;
        this.routeService = routeService;
        this.activeScenariosService = activeScenariosService;
        this.configRepository = configRepository;
        this.quantumTheory = quantumTheory;
        resourceCache = new ConcurrentLruCache<>(cacheSizeLimit, this::loadResource);
    }

    private MockResource loadResource(Route route) {
        return routeService.getEnabledRoute(route)
                .map(r -> new RestMockResource(templateEngine, r.getResponse()))
                .orElse(null);
    }

    @Override
    public void cacheRemove(Route route) {
        resourceCache.remove(route);
    }

    @Override
    public ResponseEntity<String> mock(Map<String, String> variables) {
        RequestFacade requestFacade = new RestRequestFacade(request);
        Route route = getRoute(requestFacade);
        log.info("Route requested: {}", route);
        MockResource resource = resourceCache.get(route);
        Map<String, String> requestVariables = requestFacade.getVariables(variables);
        String body = resource.getBody(requestVariables);
        if (configRepository.getSettings().getQuantum()) {
            body = quantumTheory.apply(body);
            quantumTheory.delay();
        }
        return ResponseEntity
                .status(resource.getCode())
                .headers(resource.getHeaders())
                .body(body);
    }

    private Route getRoute(RequestFacade requestFacade) {
        Route route = new Route()
                .setMethod(requestFacade.getRequestMethod())
                .setPath(requestFacade.getEndpoint());

        String alt = requestFacade
                .getAlt()
                .or(() -> getRandomAltFor(route))
                .or(() -> activeScenariosService.getAltFor(requestFacade.getRequestMethod(), requestFacade.getEndpoint()))
                .orElse("");
        return route.setAlt(alt);
    }

    private Optional<String> getRandomAltFor(Route route) {
        if (configRepository.getSettings().getRandomAlt() || configRepository.getSettings().getQuantum()) {
            return routeService.getRandomAltFor(route);
        }
        return Optional.empty();
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
