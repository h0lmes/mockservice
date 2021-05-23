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

    public void putRoute(Route route, Route replacement) throws RouteAlreadyExistsException {
        // do not allow duplicates
        if (!route.equals(replacement)) {
            Route maybeRoute = findRoute(replacement);
            if (maybeRoute != null) {
                throw new RouteAlreadyExistsException();
            }
        }

        Route maybeRoute = findRoute(route);
        if (maybeRoute == null) {
            routes.add(replacement);
        } else {
            maybeRoute.assignFrom(replacement);
        }

        routes.sort(Route::compareTo);
    }

    private Route findRoute(Route route) {
        return routes.stream()
                .filter(route::equals)
                .findFirst()
                .orElse(null);
    }

    public void deleteRoute(Route route) {
        routes.remove(route);
    }
}
