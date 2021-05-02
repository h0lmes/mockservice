package com.mockservice.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mockservice.util.MapUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class JsonHttpRequestFacade extends HttpRequestFacade {

    private static final String JSON_FILE_EXTENSION = ".json";

    public JsonHttpRequestFacade(@NonNull HttpServletRequest request,
                                 @NonNull String folder) {
        super(request, folder);
    }

    @Override
    public String getPath() {
        return "classpath:"
                + getFolder()
                + PATH_DELIMITER
                + getRequest().getMethod().toUpperCase()
                + PATH_DELIMITER_SUBSTITUTE
                + getEncodedEndpoint()
                + getMockOption()
                + JSON_FILE_EXTENSION;
    }

    @Override
    public Map<String, String> getVariables(@Nullable Map<String, String> variables) {
        Map<String, String> vars = new HashMap<>();
        vars.putAll(getBodyAsVariables());
        vars.putAll(getPathVariables());
        vars.putAll(getRequestParams());
        vars.putAll(getMockVariables());
        if (variables != null) {
            vars.putAll(variables);
        }
        return vars;
    }

    private Map<String, String> getBodyAsVariables() {
        String body = getBody();
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
