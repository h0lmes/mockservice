package com.mockachu.web.internal;

import com.mockachu.domain.Route;
import com.mockachu.domain.RouteType;
import com.mockachu.service.MockService;
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
                                  RequestMappingInfo.BuilderConfiguration options,
                                  Logger log) {
        if (route.getDisabled() || !this.getType().equals(route.getType())) {
            return;
        }

        String key = routeRegistrationKey(route);
        int regCount = Math.max(0, registeredRoutes.getOrDefault(key, 0));
        registeredRoutes.put(key, regCount + 1);
        if (regCount > 0) {
            log.info("Registering route, skipped (exists): {}", route);
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

    default void unregisterRouteInt(Route route,
                                    Map<String, Integer> registeredRoutes,
                                    RequestMappingHandlerMapping requestMappingHandlerMapping,
                                    RequestMappingInfo.BuilderConfiguration options,
                                    MockService mockService,
                                    Logger log) {
        if (route.getDisabled() || !getType().equals(route.getType())) {
            return;
        }

        mockService.cacheRemove(route);

        String key = routeRegistrationKey(route);
        int regCount = Math.max(0, registeredRoutes.getOrDefault(key, 0));
        if (regCount <= 0) {
            log.info("Unregistering route, skipped (does not exist): {}", route);
            return;
        }
        regCount--;
        registeredRoutes.put(key, regCount);
        if (regCount > 0) {
            log.info("Unregistering route, skipped (more): {}", route);
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

    default String routeRegistrationKey(Route route) {
        return route.getMethod().toString() + "-" + route.getPath();
    }
}
