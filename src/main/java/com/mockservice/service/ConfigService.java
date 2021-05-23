package com.mockservice.service;

import com.mockservice.mockconfig.Route;
import com.mockservice.mockconfig.RouteAlreadyExistsException;
import com.mockservice.mockconfig.RouteType;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface ConfigService {
    Stream<Route> getRoutes();
    Stream<Route> getEnabledRoutes();
    Stream<Route> getRoutesDistinctByPathAndMethod(RouteType type);
    Optional<Route> getEnabledRoute(RouteType routeType, RequestMethod method, String path, String suffix);
    Optional<Route> getEnabledRoute(Route route);
    List<Route> putRoute(Route route, Route replacement) throws IOException, RouteAlreadyExistsException;
    List<Route> deleteRoute(Route route) throws IOException;
}
