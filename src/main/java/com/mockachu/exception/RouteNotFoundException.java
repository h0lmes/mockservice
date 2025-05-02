package com.mockachu.exception;

import com.mockachu.domain.Route;

public class RouteNotFoundException extends RuntimeException {

    private final transient Route route;

    public RouteNotFoundException(Route route) {
        this.route = route;
    }

    @Override
    public String getMessage() {
        return route != null ? route.toString() : "";
    }

    @Override
    public String toString() {
        return route != null ? route.toString() : "";
    }
}
