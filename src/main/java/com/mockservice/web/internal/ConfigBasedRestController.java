package com.mockservice.web.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockservice.domain.Route;
import com.mockservice.domain.RouteType;
import com.mockservice.repository.ConfigObserver;
import com.mockservice.repository.ConfigRepository;
import com.mockservice.repository.RouteObserver;
import com.mockservice.request.RestRequestFacade;
import com.mockservice.service.MockService;
import com.mockservice.web.webapp.ErrorInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPatternParser;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class ConfigBasedRestController implements RouteRegisteringController, ConfigObserver, RouteObserver {

    private static final Logger log = LoggerFactory.getLogger(ConfigBasedRestController.class);

    private final HttpServletRequest request;
    private final MockService mockService;
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final ConfigRepository configRepository;
    private final Method mockMethod;
    private final ObjectMapper jsonMapper;
    private final Map<String, Integer> registeredRoutes = new ConcurrentHashMap<>();
    private final RequestMappingInfo.BuilderConfiguration options;

    public ConfigBasedRestController(HttpServletRequest request,
                                     MockService mockService,
                                     RequestMappingHandlerMapping requestMappingHandlerMapping,
                                     ConfigRepository configRepository,
                                     @Qualifier("jsonMapper") ObjectMapper jsonMapper) throws NoSuchMethodException {
        this.request = request;
        this.mockService = mockService;
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.configRepository = configRepository;
        this.jsonMapper = jsonMapper;

        mockMethod = this.getClass().getMethod("mock");

        options = new RequestMappingInfo.BuilderConfiguration();
        options.setPatternParser(new PathPatternParser());

        register();
    }

    public CompletableFuture<ResponseEntity<String>> mock() {
        RestRequestFacade facade = new RestRequestFacade(request, jsonMapper);
        return CompletableFuture.supplyAsync(() -> mockService.mock(facade));
    }

    @Override
    public RouteType getType() {
        return RouteType.REST;
    }

    private void register() {
        configRepository.findAllRoutes().forEach(this::registerRoute);
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
        this.registerRouteInt(route, registeredRoutes, mockMethod, requestMappingHandlerMapping, options, log);
    }

    private void unregisterRoute(Route route) {
        this.unregisterRouteInt(route, registeredRoutes, requestMappingHandlerMapping, options, mockService, log);
    }

    @ExceptionHandler
    protected ResponseEntity<String> handleException(Throwable t) {
        log.error("", t);
        return ResponseEntity
                .badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(mockError(t));
    }

    private String mockError(Throwable t) {
        try {
            return jsonMapper.writeValueAsString(new ErrorInfo(t));
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}
