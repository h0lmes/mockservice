package com.mockservice.service;

import com.mockservice.domain.Route;
import com.mockservice.util.JsonUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class RequestBodyValidationResult {

    private Throwable t;
    private Route route;
    private Map<String, String> variables;

    public static RequestBodyValidationResult success(Route route) {
        return new RequestBodyValidationResult(route, null, null);
    }

    public static RequestBodyValidationResult error(Route route, Throwable t) {
        Map<String, String> variables = new HashMap<>();
        variables.put("requestBodyValidationErrorMessage", JsonUtils.escape(t.toString()));
        return new RequestBodyValidationResult(route, t, variables);
    }

    private RequestBodyValidationResult(Route route, Throwable t, Map<String, String> variables) {
        this.route = route;
        this.t = t;
        this.variables = variables;
    }

    private boolean isOk() {
        return t == null;
    }

    public void ifError(Consumer<Map<String, String>> consumer) {
        if (!isOk()) {
            Objects.requireNonNull(variables, "RequestBodyValidationResult has error but variables are null!");
            consumer.accept(variables);
        }
    }

    public Route getRoute() {
        return route;
    }
}
