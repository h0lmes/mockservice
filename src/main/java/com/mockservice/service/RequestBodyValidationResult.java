package com.mockservice.service;

import com.mockservice.domain.Route;

import java.util.Map;

public class RequestBodyValidationResult {

    private Exception e = null;
    private Route route = null;
    private Map<String, String> variables = null;

    public RequestBodyValidationResult() {
        // default
    }

    public RequestBodyValidationResult(Exception e, Route route, Map<String, String> variables) {
        this.e = e;
        this.route = route;
        this.variables = variables;
    }

    public boolean isOk() {
        return e == null;
    }

    public Exception getE() {
        return e;
    }

    public Route getRoute() {
        return route;
    }

    public Map<String, String> getVariables() {
        return variables;
    }
}
