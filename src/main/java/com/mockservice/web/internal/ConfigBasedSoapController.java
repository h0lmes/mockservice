package com.mockservice.web.internal;

import com.mockservice.domain.Route;
import com.mockservice.domain.RouteType;
import com.mockservice.service.MockService;
import com.mockservice.service.RouteService;
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
public class ConfigBasedSoapController implements RouteRegisteringController {

    private static final Logger log = LoggerFactory.getLogger(ConfigBasedSoapController.class);

    private final MockService mockService;
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final RouteService routeService;
    private Method mockMethod = null;
    private final Map<String, Integer> registeredRoutes = new ConcurrentHashMap<>();

    public ConfigBasedSoapController(@Qualifier("soap") MockService mockService,
                                     RequestMappingHandlerMapping requestMappingHandlerMapping,
                                     RouteService routeService) {
        this.mockService = mockService;
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.routeService = routeService;

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
        return RouteType.SOAP;
    }

    private void register() {
        routeService.getRoutes().forEach(this::registerRoute);

        routeService.registerRouteCreatedListener(this::registerRoute);
        routeService.registerRouteDeletedListener(this::unregisterRoute);
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
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mockService.mockError(t));
    }
}
