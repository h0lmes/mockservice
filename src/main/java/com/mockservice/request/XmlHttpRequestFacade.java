package com.mockservice.request;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class XmlHttpRequestFacade extends HttpRequestFacade {

    private static final String XML_FILE_EXTENSION = ".xml";

    public XmlHttpRequestFacade(@NonNull HttpServletRequest request,
                                @NonNull String folder) {
        super(request, folder);
    }

    @Override
    public String getPath() {
        return "classpath:"
                + getFolder()
                + PATH_DELIMITER
                + getEncodedEndpoint()
                + getMockOption()
                + XML_FILE_EXTENSION;
    }

    @Override
    public Map<String, String> getVariables(@Nullable Map<String, String> variables) {
        Map<String, String> vars = new HashMap<>();
        vars.putAll(getPathVariables());
        vars.putAll(getRequestParams());
        vars.putAll(getMockVariables());
        if (variables != null) {
            vars.putAll(variables);
        }
        return vars;
    }
}
