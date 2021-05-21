package com.mockservice.service;

import com.mockservice.mockconfig.Config;
import com.mockservice.mockconfig.Route;
import com.mockservice.mockconfig.RouteType;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

public interface ConfigService {
    Config getConfig();
    Stream<Route> getEnabledRoutes();
    Stream<Route> getRoutesDistinctByPathAndMethod(RouteType type);
    Optional<Route> getEnabledRoute(RouteType routeType, RequestMethod method, String path, String suffix);
    Optional<Route> getEnabledRoute(Route route);
    Config putRoute(Route route) throws IOException;
    Config deleteRoute(Route route) throws IOException;
}
