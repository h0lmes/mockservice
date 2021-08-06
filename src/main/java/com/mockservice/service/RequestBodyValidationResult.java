package com.mockservice.service;

import com.mockservice.domain.Route;

import java.util.Map;

public class RequestBodyValidationResult {

    private Exception e;
    private Route route;
    private Map<String, String> variables;

    public static RequestBodyValidationResult success(Route route) {
        return new RequestBodyValidationResult(null, route, null);
    }

    public static RequestBodyValidationResult error(Exception e, Route route, Map<String, String> variables) {
        return new RequestBodyValidationResult(e, route, variables);
    }

    private RequestBodyValidationResult(Exception e, Route route, Map<String, String> variables) {
        this.e = e;
        this.route = route;
        this.variables = variables;
    }

    public boolean isOk() {
        return e == null;
    }

    public Exception getException() {
        return e;
    }

    public Route getRoute() {
        return route;
    }

    public Map<String, String> getVariables() {
        return variables;
    }
}
