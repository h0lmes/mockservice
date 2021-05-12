package com.mockservice.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mockservice.util.MapUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class RestRequestFacade extends AbstractRequestFacade {

    private static final String JSON_FILE_EXTENSION = ".json";

    public RestRequestFacade(@NonNull HttpServletRequest request,
                             @NonNull String service) {
        super(request, service);
    }

    @Override
    public String getPath() {
        return getService()
                + File.separator
                + getRequest().getMethod().toLowerCase()
                + NAME_DELIMITER
                + getEndpoint()
                + getOption()
                + JSON_FILE_EXTENSION;
    }

    @Override
    public Map<String, String> getVariables(@Nullable Map<String, String> variables) {
        Map<String, String> vars = new HashMap<>();
        vars.putAll(getBodyAsVariables());
        vars.putAll(getPathVariables());
        vars.putAll(getRequestParams());
        vars.putAll(getHeaderVariables());
        if (variables != null) {
            vars.putAll(variables);
        }
        return vars;
    }

    private Map<String, String> getBodyAsVariables() {
        String body = getBody().collect(Collectors.joining(System.lineSeparator()));
        if (body != null && !body.isEmpty()) {
            try {
                return MapUtils.flattenMap(MapUtils.jsonToMap(body));
            } catch (JsonProcessingException e) {
                // Not a valid JSON. Ignore.
            }
        }
        return new HashMap<>();
    }
}
