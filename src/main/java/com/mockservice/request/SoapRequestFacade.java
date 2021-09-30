package com.mockservice.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockservice.util.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SoapRequestFacade extends AbstractRequestFacade {

    private static final Logger log = LoggerFactory.getLogger(SoapRequestFacade.class);

    public SoapRequestFacade(HttpServletRequest request, ObjectMapper jsonMapper) {
        super(request, jsonMapper);
    }

    @Override
    public Map<String, String> getVariables(Optional<Map<String, String>> baseVariables) {
        Map<String, String> vars = new HashMap<>();
        baseVariables.ifPresent(vars::putAll);
        vars.putAll(getBodyAsVariables());
        vars.putAll(getHeaderVariables());
        return vars;
    }

    private Map<String, String> getBodyAsVariables() {
        String body = getBody();
        try {
            return MapUtils.flattenMap(MapUtils.xmlToMap(body));
        } catch (Exception e) {
            log.warn("Not a valid XML:\n{}", body);
        }
        return new HashMap<>();
    }
}
