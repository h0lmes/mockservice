package com.mockservice.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mockservice.util.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class RestRequestFacade extends AbstractRequestFacade {

    private static final Logger log = LoggerFactory.getLogger(RestRequestFacade.class);

    public RestRequestFacade(HttpServletRequest request) {
        super(request);
    }

    @Override
    public Map<String, String> getVariables() {
        Map<String, String> vars = new HashMap<>();
        vars.putAll(getBodyAsVariables());
        vars.putAll(getPathVariables());
        vars.putAll(getRequestParams());
        vars.putAll(getHeaderVariables());
        return vars;
    }

    private Map<String, String> getBodyAsVariables() {
        String body = getBody();
        if (body != null && !body.isEmpty()) {
            try {
                return MapUtils.flattenMap(MapUtils.jsonToMap(body));
            } catch (JsonProcessingException e) {
                log.warn("Not a valid JSON:\n{}", body);
            }
        }
        return new HashMap<>();
    }
}
