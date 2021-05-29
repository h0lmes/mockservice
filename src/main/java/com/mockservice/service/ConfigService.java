package com.mockservice.service;

import com.mockservice.mockconfig.Route;
import com.mockservice.mockconfig.RouteAlreadyExistsException;
import com.mockservice.mockconfig.RouteType;
import com.mockservice.model.PlainConfig;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface ConfigService {
    PlainConfig getConfigData();
    void writeConfigData(PlainConfig config) throws IOException;
    Stream<Route> getRoutes();
    Stream<Route> getEnabledRoutes();
    Optional<Route> getEnabledRoute(RouteType routeType, RequestMethod method, String path, String suffix);
    Optional<Route> getEnabledRoute(Route route);
    List<Route> putRoute(Route route, Route replacement) throws IOException, RouteAlreadyExistsException;
    List<Route> deleteRoute(Route route) throws IOException;
    void registerRouteCreatedListener(Consumer<Route> listener);
    void registerRouteDeletedListener(Consumer<Route> listener);
}
