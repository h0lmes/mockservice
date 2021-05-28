package com.mockservice.web.internal;

import com.mockservice.mockconfig.Route;
import com.mockservice.mockconfig.RouteType;
import com.mockservice.service.MockService;
import org.slf4j.Logger;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.Map;

public interface RouteRegisteringController {

    RouteType getType();

    default void registerRouteInt(Route route,
                                  Map<String, Integer> registeredRoutes,
                                  Method mockMethod,
                                  RequestMappingHandlerMapping requestMappingHandlerMapping,
                                  Logger log) {
        if (route.getDisabled() || !this.getType().equals(route.getType())) {
            return;
        }

        if (mockMethod == null) {
            log.warn("Register route (no method): {}", route);
        }

        String key = routeRegistrationKey(route);
        int regCount = Math.max(0, registeredRoutes.getOrDefault(key, 0));
        registeredRoutes.put(key, regCount + 1);
        if (regCount > 0) {
            log.info("Register route (skip):    {} ", route);
            return;
        }

        RequestMappingInfo mappingInfo = RequestMappingInfo
                .paths(route.getPath())
                .methods(route.getMethod())
                .build();
        requestMappingHandlerMapping.registerMapping(mappingInfo, this, mockMethod);

        log.info("Register route (success): {}", route);
    }

    default void unregisterRouteInt(Route route,
                                    Map<String, Integer> registeredRoutes,
                                    RequestMappingHandlerMapping requestMappingHandlerMapping,
                                    MockService mockService,
                                    Logger log) {
        if (route.getDisabled() || !getType().equals(route.getType())) {
            return;
        }

        String key = routeRegistrationKey(route);
        int regCount = Math.max(0, registeredRoutes.getOrDefault(key, 0));
        if (regCount <= 0) {
            log.info("Unregister route (skip - no registration): {} ", route);
            return;
        }
        regCount--;
        registeredRoutes.put(key, regCount);
        if (regCount > 0) {
            log.info("Unregister route (skip - more): {} ", route);
            return;
        }

        RequestMappingInfo mappingInfo = RequestMappingInfo
                .paths(route.getPath())
                .methods(route.getMethod())
                .build();
        requestMappingHandlerMapping.unregisterMapping(mappingInfo);

        mockService.cacheRemove(route);

        log.info("Unregister route (success): {}", route);
    }

    default String routeRegistrationKey(Route route) {
        return route.getMethod().toString() + "-" + route.getPath();
    }
}
