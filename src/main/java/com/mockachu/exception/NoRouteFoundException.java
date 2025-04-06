package com.mockachu.exception;

import com.mockachu.domain.Route;

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
