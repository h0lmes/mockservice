package com.mockservice.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mockservice.util.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
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
        vars.putAll(getAuthorizationAsVariables());
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
                log.warn("Invalid JSON:\n{}", body);
            }
        }
        return new HashMap<>();
    }

    private Map<String, String> getAuthorizationAsVariables() {
        if (!authHeaders.isEmpty()) {
            String token = authHeaders.get(0)[0];
            if ("bearer".equalsIgnoreCase(token.substring(0, 6))) {
                token = token.substring(7);
                String[] chunks = token.split("\\.");
                if (chunks.length > 1) {
                    Base64.Decoder decoder = Base64.getDecoder();
                    String payload = new String(decoder.decode(chunks[1]));
                    try {
                        return MapUtils.flattenMap(MapUtils.jsonToMap(payload));
                    } catch (JsonProcessingException e) {
                        log.warn("Invalid JWT payload:\n{}", payload);
                    }
                }
            }
        }
        return new HashMap<>();
    }
}
