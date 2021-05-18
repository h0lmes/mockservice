package com.mockservice.service;

import com.mockservice.mockconfig.Config;
import com.mockservice.mockconfig.Route;
import com.mockservice.mockconfig.RouteType;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Optional;
import java.util.stream.Stream;

public interface ConfigService {
    Config getConfig();
    Stream<Route> getEnabledRoutes();
    Optional<Route> getEnabledRoute(RouteType routeType, RequestMethod method, String path, String suffix);
}
