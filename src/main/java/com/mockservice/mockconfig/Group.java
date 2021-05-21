package com.mockservice.mockconfig;

import java.util.ArrayList;
import java.util.List;

public class Group {

    private String name = "";
    private List<Route> routes = new ArrayList<>();

    public Group() {
        // default
    }

    public String getName() {
        return name;
    }

    public Group setName(String name) {
        this.name = name;
        return this;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public Group setRoutes(List<Route> routes) {
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
