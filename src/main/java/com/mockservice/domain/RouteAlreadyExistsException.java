package com.mockservice.domain;

public class RouteAlreadyExistsException extends Exception {

    private final Route route;

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
