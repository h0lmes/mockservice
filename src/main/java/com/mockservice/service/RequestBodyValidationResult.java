package com.mockservice.service;

import com.mockservice.domain.Route;

import java.util.Map;

public class RequestBodyValidationResult {

    private Exception e = null;
    private Route route = null;
    private Map<String, String> variables = null;

    public static RequestBodyValidationResult success(Route route, Map<String, String> variables) {
        return new RequestBodyValidationResult(null, route, variables);
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
