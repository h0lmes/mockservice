package com.mockservice.service;

import com.mockservice.domain.Route;

public class NoRouteFoundException extends RuntimeException {

    private final Route route;

    public NoRouteFoundException(Route route) {
        this.route = route;
    }

    @Override
    public String toString() {
        return route != null ? route.toString() : "";
    }
}
