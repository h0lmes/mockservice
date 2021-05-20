package com.mockservice.request;

import org.springframework.lang.Nullable;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class SoapRequestFacade extends AbstractRequestFacade {

    public SoapRequestFacade(HttpServletRequest request) {
        super(request);
    }

    @Override
    public Map<String, String> getVariables(@Nullable Map<String, String> variables) {
        Map<String, String> vars = new HashMap<>();
        vars.putAll(getPathVariables());
        vars.putAll(getRequestParams());
        vars.putAll(getHeaderVariables());
        if (variables != null) {
            vars.putAll(variables);
        }
        return vars;
    }
}
