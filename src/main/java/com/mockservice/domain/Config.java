package com.mockservice.domain;

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

    public boolean putRoute(Route route, Route replacement) throws RouteAlreadyExistsException {
        // do not allow duplicates
        if (!route.equals(replacement)) {
            Route maybeRoute = findRoute(replacement);
            if (maybeRoute != null) {
                throw new RouteAlreadyExistsException(replacement);
            }
        }

        boolean updated = false;
        Route maybeRoute = findRoute(route);
        if (maybeRoute == null) {
            routes.add(replacement);
        } else {
            maybeRoute.assignFrom(replacement);
            updated = true;
        }

        routes.sort(Route::compareTo);
        return updated;
    }

    private Route findRoute(Route route) {
        return routes.stream()
                .filter(route::equals)
                .findFirst()
                .orElse(null);
    }

    public boolean deleteRoute(Route route) {
        return routes.remove(route);
    }
}
