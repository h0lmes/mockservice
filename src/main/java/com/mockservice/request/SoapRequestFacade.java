package com.mockservice.request;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SoapRequestFacade extends AbstractRequestFacade {

    private static final String XML_FILE_EXTENSION = ".xml";

    public SoapRequestFacade(@NonNull HttpServletRequest request,
                             @NonNull String service) {
        super(request, service);
    }

    @Override
    public String getPath() {
        return getService()
                + File.separator
                + getEndpoint()
                + getOption()
                + XML_FILE_EXTENSION;
    }

    @Override
    public Map<String, String> getVariables(@Nullable Map<String, String> variables) {
        Map<String, String> vars = new HashMap<>();
        vars.putAll(getPathVariables());
        vars.putAll(getRequestParams());
        vars.putAll(getHeaderVariables());
        if (variables != null) {
            vars.putAll(variables);
        }
        return vars;
    }
}
