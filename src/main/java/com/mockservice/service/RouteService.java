package com.mockservice.service;

import com.mockservice.domain.Route;
import com.mockservice.domain.RouteAlreadyExistsException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface RouteService {
    Optional<Route> getEnabledRoute(Route route);
    Optional<String> getRandomAltFor(Route route);
    List<Route> getRoutesAsList();
    List<Route> putRoute(Route route, Route replacement) throws IOException, RouteAlreadyExistsException;
    List<Route> deleteRoute(Route route) throws IOException;
}
