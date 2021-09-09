package com.mockservice.service.route;

import com.mockservice.domain.Route;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.web.bind.annotation.RequestMethod;

public interface RouteService {
    Optional<Route> getEnabledRoute(Route route);
    Optional<String> getRandomAltFor(RequestMethod method, String path);
    List<RouteDto> getRoutes();
    void putRoute(RouteDto dto) throws IOException;
    void putRoutes(List<RouteDto> dtos, boolean overwrite) throws IOException;
    void deleteRoutes(List<RouteDto> dtos) throws IOException;
    RouteVariableDto setRouteVariable(RouteVariableDto variable);
    RouteVariableDto clearRouteVariable(RouteVariableDto variable);
    Map<String, String> getRouteVariables(Route route);
}
