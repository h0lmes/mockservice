package com.mockservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mockservice.domain.Route;
import com.mockservice.domain.RouteAlreadyExistsException;
import com.mockservice.domain.RouteType;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface ConfigService {
    String getConfigData() throws JsonProcessingException;
    void writeConfigData(String data) throws IOException;
    Stream<Route> getRoutes();
    Stream<Route> getEnabledRoutes();
    Optional<Route> getEnabledRoute(RouteType routeType, RequestMethod method, String path, String suffix);
    Optional<Route> getEnabledRoute(Route route);
    List<Route> putRoute(Route route, Route replacement) throws IOException, RouteAlreadyExistsException;
    List<Route> deleteRoute(Route route) throws IOException;
    void registerRouteCreatedListener(Consumer<Route> listener);
    void registerRouteDeletedListener(Consumer<Route> listener);
}
