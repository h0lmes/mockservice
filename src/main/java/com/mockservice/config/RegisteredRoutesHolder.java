package com.mockservice.config;

import com.mockservice.mockconfig.Route;
import com.mockservice.mockconfig.RouteType;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Optional;

public interface RegisteredRoutesHolder {
    Optional<Route> getRegisteredRoute(RouteType routeType, RequestMethod method, String path, String suffix);
}
