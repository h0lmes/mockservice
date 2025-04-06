package com.mockachu.service;

import com.mockachu.domain.Route;
import com.mockachu.model.RouteDto;
import com.mockachu.template.MockVariables;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface RouteService {
    Optional<Route> getEnabledRoute(Route route);
    Optional<String> getRandomAltFor(RequestMethod method, String path);
    Optional<Route> getRouteForVariables(RequestMethod method, String path, MockVariables variables);
    List<RouteDto> getRoutes();
    void putRoute(RouteDto reference, RouteDto route) throws IOException;
    void putRoutes(List<RouteDto> dto, boolean overwrite) throws IOException;
    void deleteRoutes(List<RouteDto> dto) throws IOException;
}
