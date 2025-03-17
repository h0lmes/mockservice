package com.mockservice.exception;

import com.mockservice.domain.Route;

public class RouteAlreadyExistsException extends RuntimeException {

    private final transient Route route;

    public RouteAlreadyExistsException(Route route) {
        this.route = route;
    }

    @Override
    public String getMessage() {
        return "Route already exists: " + route;
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
