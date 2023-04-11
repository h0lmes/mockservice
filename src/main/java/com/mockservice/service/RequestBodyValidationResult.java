package com.mockservice.service;

import com.mockservice.domain.Route;
import com.mockservice.template.MockVariables;
import com.mockservice.util.JsonUtils;

import java.util.Objects;
import java.util.function.Consumer;

public class RequestBodyValidationResult {

    private final Throwable t;
    private final Route route;
    private final MockVariables variables;

    public static RequestBodyValidationResult success(Route route) {
        return new RequestBodyValidationResult(route, null, null);
    }

    public static RequestBodyValidationResult error(Route route, Throwable t) {
        MockVariables variables = new MockVariables();
        variables.put("requestBodyValidationErrorMessage", JsonUtils.escape(t.toString()));
        return new RequestBodyValidationResult(route, t, variables);
    }

    private RequestBodyValidationResult(Route route, Throwable t, MockVariables variables) {
        this.route = route;
        this.t = t;
        this.variables = variables;
    }

    private boolean isOk() {
        return t == null;
    }

    public void ifError(Consumer<MockVariables> consumer) {
        if (!isOk()) {
            Objects.requireNonNull(variables, "RequestBodyValidationResult has error but variables are null!");
            consumer.accept(variables);
        }
    }

    public Route getRoute() {
        return route;
    }
}
