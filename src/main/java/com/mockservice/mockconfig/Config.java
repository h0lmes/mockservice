package com.mockservice.mockconfig;

import java.util.ArrayList;
import java.util.List;

public class Config {

    private List<Route> routes = new ArrayList<>();

    public Config() {
        // default
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public Config setRoutes(List<Route> routes) {
        this.routes = routes;
        return this;
    }

    public void putRoute(Route route) {
        Route maybeRoute = routes.stream()
                .filter(route::equals)
                .findFirst()
                .orElse(null);
        if (maybeRoute == null) {
            maybeRoute = route;
            routes.add(maybeRoute);
        } else {
            maybeRoute.setDisabled(route.getDisabled());
            maybeRoute.setResponse(route.getResponse());
        }
    }

    public void deleteRoute(Route route) {
        routes.remove(route);
    }
}
