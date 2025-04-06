package com.mockservice.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockservice.template.MockVariables;
import com.mockservice.util.MapUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.util.Base64;
import java.util.Optional;

public class RestRequestFacade extends AbstractRequestFacade {

    private static final Logger log = LoggerFactory.getLogger(RestRequestFacade.class);

    public RestRequestFacade(HttpServletRequest request, ObjectMapper jsonMapper) {
        super(request, jsonMapper);
    }

    @Override
    public MockVariables getVariables(@Nullable MockVariables baseVariables) {
        MockVariables vars = new MockVariables();
        if (baseVariables != null) vars.putAll(baseVariables);
        getAuthorizationAsVariables().ifPresent(vars::putAll);
        getBodyAsVariables().ifPresent(vars::putAll);
        vars.putAll(getPathVariables());
        vars.putAll(getRequestParams());
        vars.putAll(getHeaderVariables());
        return vars;
    }

    private Optional<MockVariables> getBodyAsVariables() {
        String body = getBody();
        try {
            MockVariables vars = new MockVariables();
            vars.putAll(MapUtils.flattenMap(MapUtils.jsonToMap(body, jsonMapper)));
            return Optional.of(vars);
        } catch (JsonProcessingException e) {
            log.warn("Invalid JSON:\n{}", body);
        }
        return Optional.empty();
    }

    private Optional<MockVariables> getAuthorizationAsVariables() {
        if (!authHeaders.isEmpty()) {
            String token = authHeaders.get(0)[0];
            if ("bearer".equalsIgnoreCase(token.substring(0, 6))) {
                token = token.substring(7);
                String[] chunks = token.split("\\.");
                if (chunks.length > 1) {
                    Base64.Decoder decoder = Base64.getDecoder();
                    String payload = new String(decoder.decode(chunks[1]));
                    try {
                        MockVariables vars = new MockVariables();
                        vars.putAll(MapUtils.flattenMap(MapUtils.jsonToMap(payload, jsonMapper)));
                        return Optional.of(vars);
                    } catch (JsonProcessingException e) {
                        log.warn("Invalid JWT payload:\n{}", payload);
                    }
                }
            }
        }
        return Optional.empty();
    }
}
