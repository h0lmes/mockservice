package com.mockservice.request;

import org.springframework.lang.Nullable;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SoapRequestFacade extends AbstractRequestFacade {

    private static final String XML_FILE_EXTENSION = ".xml";

    public SoapRequestFacade(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getPath(String group) {
        return group
                + File.separator
                + getEncodedEndpoint()
                + getSuffix(group)
                + XML_FILE_EXTENSION;
    }

    @Override
    public Map<String, String> getVariables(String group, @Nullable Map<String, String> variables) {
        Map<String, String> vars = new HashMap<>();
        vars.putAll(getPathVariables());
        vars.putAll(getRequestParams());
        vars.putAll(getHeaderVariables(group));
        if (variables != null) {
            vars.putAll(variables);
        }
        return vars;
    }
}
