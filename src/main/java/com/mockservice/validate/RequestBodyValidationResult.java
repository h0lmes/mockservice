package com.mockservice.validate;

import com.mockservice.domain.Route;
import com.mockservice.template.MockVariables;
import com.mockservice.util.JsonUtils;

import java.util.Objects;
import java.util.function.Consumer;

public class RequestBodyValidationResult {

    private final Throwable throwable;
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

    private RequestBodyValidationResult(Route route, Throwable throwable, MockVariables variables) {
        this.route = route;
        this.throwable = throwable;
        this.variables = variables;
    }

    public void ifError(Consumer<MockVariables> consumer) {
        if (throwable != null) {
            Objects.requireNonNull(variables, "RequestBodyValidationResult has error but variables are null!");
            consumer.accept(variables);
        }
    }

    public Route getRoute() {
        return route;
    }
}
