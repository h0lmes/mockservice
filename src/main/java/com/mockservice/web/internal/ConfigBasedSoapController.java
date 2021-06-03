package com.mockservice.web.internal;

import com.mockservice.domain.Route;
import com.mockservice.domain.RouteType;
import com.mockservice.service.ConfigChangedListener;
import com.mockservice.service.ConfigRepository;
import com.mockservice.service.MockService;
import com.mockservice.service.RoutesChangedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class ConfigBasedSoapController implements RouteRegisteringController, ConfigChangedListener, RoutesChangedListener {

    private static final Logger log = LoggerFactory.getLogger(ConfigBasedSoapController.class);

    private final MockService mockService;
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final ConfigRepository configRepository;
    private Method mockMethod = null;
    private final Map<String, Integer> registeredRoutes = new ConcurrentHashMap<>();

    public ConfigBasedSoapController(@Qualifier("soap") MockService mockService,
                                     RequestMappingHandlerMapping requestMappingHandlerMapping,
                                     ConfigRepository configRepository) {
        this.mockService = mockService;
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.configRepository = configRepository;

        try {
            mockMethod = this.getClass().getMethod("mock");
        } catch (NoSuchMethodException e) {
            log.error("", e);
        }

        try {
            register();
        } catch (Exception e) {
            log.error("Failed to register configured routes.", e);
        }
    }

    public ResponseEntity<String> mock() {
        return mockService.mock(null);
    }

    @Override
    public RouteType getType() {
        return RouteType.SOAP;
    }

    private void register() {
        configRepository.findAllRoutes().forEach(this::registerRoute);
        configRepository.registerRoutesChangedListener(this);
        configRepository.registerConfigChangedListener(this);
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
        this.registerRouteInt(route, registeredRoutes, mockMethod, requestMappingHandlerMapping, log);
    }

    private void unregisterRoute(Route route) {
        this.unregisterRouteInt(route, registeredRoutes, requestMappingHandlerMapping, mockService, log);
    }

    @ExceptionHandler
    protected ResponseEntity<String> handleException(Throwable t) {
        log.error("", t);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mockService.mockError(t));
    }
}
