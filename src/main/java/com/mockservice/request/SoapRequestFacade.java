package com.mockservice.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockservice.template.MockVariables;
import com.mockservice.util.MapUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class SoapRequestFacade extends AbstractRequestFacade {

    private static final Logger log = LoggerFactory.getLogger(SoapRequestFacade.class);

    public SoapRequestFacade(HttpServletRequest request, ObjectMapper jsonMapper) {
        super(request, jsonMapper);
    }

    @Override
    public MockVariables getVariables(Optional<MockVariables> baseVariables) {
        MockVariables vars = new MockVariables();
        baseVariables.ifPresent(vars::putAll);
        getBodyAsVariables().ifPresent(vars::putAll);
        vars.putAll(getHeaderVariables());
        return vars;
    }

    private Optional<MockVariables> getBodyAsVariables() {
        String body = getBody();
        try {
            MockVariables vars = new MockVariables();
            vars.putAll(MapUtils.flattenMap(MapUtils.xmlToMap(body)));
            return Optional.of(vars);
        } catch (Exception e) {
            log.warn("Not a valid XML:\n{}", body);
        }
        return Optional.empty();
    }
}
