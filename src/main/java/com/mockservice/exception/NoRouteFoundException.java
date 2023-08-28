package com.mockservice.exception;

import com.mockservice.domain.Route;

public class NoRouteFoundException extends RuntimeException {

    private final transient Route route;

    public NoRouteFoundException(Route route) {
        this.route = route;
    }

    @Override
    public String toString() {
        return route != null ? route.toString() : "";
    }
}
