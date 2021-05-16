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

    public RestRequestFacade(@NonNull String group, @NonNull HttpServletRequest request) {
        super(group, request);
    }

    @Override
    public String getPath() {
        return getGroup()
                + File.separator
                + getMethod()
                + NAME_DELIMITER
                + getEndpoint()
                + getSuffix()
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
