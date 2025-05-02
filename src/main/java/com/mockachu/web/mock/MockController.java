package com.mockachu.web.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockachu.domain.Route;
import com.mockachu.exception.RouteNotFoundException;
import com.mockachu.repository.ConfigObserver;
import com.mockachu.repository.ConfigRepository;
import com.mockachu.repository.RouteObserver;
import com.mockachu.service.HttpService;
import com.mockachu.service.MockService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPatternParser;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class MockController implements ConfigObserver, RouteObserver {

    private static final Logger log = LoggerFactory.getLogger(MockController.class);

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final HttpServletRequest request;
    private final MockService mockService;
    private final HttpService httpService;
    private final ConfigRepository configRepository;
    private final Method mockMethod;
    private final ObjectMapper jsonMapper;
    private final Map<String, Integer> registeredRoutes = new ConcurrentHashMap<>();
    private final RequestMappingInfo.BuilderConfiguration options;

    public MockController(RequestMappingHandlerMapping requestMappingHandlerMapping,
                          HttpServletRequest request,
                          MockService mockService,
                          HttpService httpService, ConfigRepository configRepository,
                          @Qualifier("jsonMapper") ObjectMapper jsonMapper) throws NoSuchMethodException {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.request = request;
        this.mockService = mockService;
        this.httpService = httpService;
        this.configRepository = configRepository;
        this.configRepository.registerConfigObserver(this);
        this.configRepository.registerRouteObserver(this);
        this.jsonMapper = jsonMapper;

        mockMethod = this.getClass().getMethod("mock");

        options = new RequestMappingInfo.BuilderConfiguration();
        options.setPatternParser(new PathPatternParser());

        register();
    }

    public CompletableFuture<ResponseEntity<String>> mock() {
        var facade = new MockRequestFacade(this.request, jsonMapper);
        return CompletableFuture.supplyAsync(() -> mockWithProxy(facade));
    }

    private ResponseEntity<String> mockWithProxy(MockRequestFacade facade) {
        try {
            return mockService.mock(facade);
        } catch (RouteNotFoundException e) {
            if (!configRepository.getSettings().isProxyEnabled()) throw e;
            return proxyRequest(facade);
        }
    }

    private ResponseEntity<String> proxyRequest(MockRequestFacade facade) {
        log.info("Proxying request: {}", facade);

        String proxyUri = configRepository.getSettings().getProxyLocation();
        if (proxyUri == null) proxyUri = "";
        if (!proxyUri.isBlank() && proxyUri.endsWith("/")) {
            proxyUri = proxyUri.substring(0, proxyUri.length() - 1);
        }
        proxyUri += facade.getUriAndQueryString();

        var result = httpService.request(
                facade.getMethod(), proxyUri, facade.getBody(), facade.getHeaders());

        return ResponseEntity.status(result.getStatusCode())
                .headers(result.getResponseHeaders())
                .body(result.getResponseBody());
    }

    private void register() {
        configRepository.findAllRoutes().forEach(this::registerRoute);

        // capture all other requests
        registerRoute(new Route().setMethod(RequestMethod.GET).setPath("/**"));
        registerRoute(new Route().setMethod(RequestMethod.POST).setPath("/**"));
        registerRoute(new Route().setMethod(RequestMethod.PUT).setPath("/**"));
        registerRoute(new Route().setMethod(RequestMethod.PATCH).setPath("/**"));
        registerRoute(new Route().setMethod(RequestMethod.DELETE).setPath("/**"));
    }

    @Override
    public void onBeforeConfigChanged() {
        configRepository.findAllRoutes().forEach(this::unregisterRoute);
    }

    @Override
    public void onAfterConfigChanged() {
        configRepository.findAllRoutes().forEach(this::registerRoute);
    }

    @Override
    public void onRouteCreated(Route route) {
        registerRoute(route);
    }

    @Override
    public void onRouteDeleted(Route route) {
        unregisterRoute(route);
    }

    private void registerRoute(Route route) {
        if (route.getDisabled()) {
            return;
        }

        String key = routeKey(route);
        int regCount = Math.max(0, registeredRoutes.getOrDefault(key, 0));
        registeredRoutes.put(key, regCount + 1);
        if (regCount > 0) {
            log.info("Registering route, success: {} --- exists ---", route);
            return;
        }

        RequestMappingInfo mappingInfo = RequestMappingInfo
                .paths(route.getPath())
                .methods(route.getMethod())
                .options(options)
                .build();
        requestMappingHandlerMapping.registerMapping(mappingInfo, this, mockMethod);

        log.info("Registering route, success: {}", route);
    }

    private void unregisterRoute(Route route) {
        if (route.getDisabled()) {
            return;
        }

        mockService.cacheRemove(route);

        String key = routeKey(route);
        int regCount = Math.max(0, registeredRoutes.getOrDefault(key, 0));
        if (regCount <= 0) {
            log.info("Unregistering route, skipped: {} --- does not exist ---", route);
            return;
        }
        regCount--;
        registeredRoutes.put(key, regCount);
        if (regCount > 0) {
            log.info("Unregistering route, success: {} --- more ---", route);
            return;
        }

        RequestMappingInfo mappingInfo = RequestMappingInfo
                .paths(route.getPath())
                .methods(route.getMethod())
                .options(options)
                .build();
        requestMappingHandlerMapping.unregisterMapping(mappingInfo);

        log.info("Unregistering route, success: {}", route);
    }

    private String routeKey(Route route) {
        return route.getMethod().toString() + "-" + route.getPath();
    }

    @ExceptionHandler(RouteNotFoundException.class)
    protected ResponseEntity<String> handleRouteNotFoundException(RouteNotFoundException e) {
        log.error("Route not found: {}", e.getMessage());
        return ResponseEntity.status(404).contentType(MediaType.TEXT_PLAIN).body(e.getMessage());
    }

    @ExceptionHandler
    protected ResponseEntity<String> handleException(Throwable t) {
        log.error("", t);
        return ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN).body(t.getMessage());
    }
}
