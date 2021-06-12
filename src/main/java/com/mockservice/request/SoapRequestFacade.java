package com.mockservice.request;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class SoapRequestFacade extends AbstractRequestFacade {

    public SoapRequestFacade(HttpServletRequest request) {
        super(request);
    }

    @Override
    public Map<String, String> getVariables() {
        Map<String, String> vars = new HashMap<>();
        vars.putAll(getHeaderVariables());
        return vars;
    }
}
