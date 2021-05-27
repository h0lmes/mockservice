package com.mockservice.web.internal;

import com.mockservice.mockconfig.Route;
import com.mockservice.mockconfig.RouteType;
import com.mockservice.service.ConfigService;
import com.mockservice.service.MockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class ConfigBasedRestController implements RouteRegisteringController {

    private static final Logger log = LoggerFactory.getLogger(ConfigBasedRestController.class);

    private final MockService mockService;
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final ConfigService configService;
    private Method mockMethod = null;
    private final Map<String, Integer> registeredRoutes = new ConcurrentHashMap<>();

    public ConfigBasedRestController(@Qualifier("rest") MockService mockService,
                                     RequestMappingHandlerMapping requestMappingHandlerMapping,
                                     ConfigService configService) {
        this.mockService = mockService;
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.configService = configService;

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

    @Override
    public RouteType getType() {
        return RouteType.REST;
    }

    private void register() {
        configService.getRoutes().forEach(this::registerRoute);

        configService.registerRouteCreatedListener(this::registerRoute);
        configService.registerRouteDeletedListener(this::unregisterRoute);
    }

    private void registerRoute(Route route) {
        this.registerRouteInt(route, registeredRoutes, mockMethod, requestMappingHandlerMapping, log);
    }

    private void unregisterRoute(Route route) {
        this.unregisterRouteInt(route, registeredRoutes, requestMappingHandlerMapping, mockService, log);
    }

    public ResponseEntity<String> mock() {
        return mockService.mock(null);
    }

    @ExceptionHandler
    protected ResponseEntity<String> handleException(Throwable t) {
        log.error("", t);
        return ResponseEntity
                .badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(mockService.mockError(t));
    }
}
