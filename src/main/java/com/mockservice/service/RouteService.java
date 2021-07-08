package com.mockservice.service;

import com.mockservice.domain.Route;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface RouteService {
    Optional<Route> getEnabledRoute(Route route);
    Optional<String> getRandomAltFor(Route route);
    List<Route> getRoutesAsList();
    List<Route> putRoute(Route route) throws IOException;
    List<Route> putRoutes(List<Route> routes, boolean overwrite) throws IOException;
    List<Route> deleteRoutes(List<Route> routes) throws IOException;
}
