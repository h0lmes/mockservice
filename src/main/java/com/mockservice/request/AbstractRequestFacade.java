package com.mockservice.request;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractRequestFacade implements RequestFacade {

    private static final String REQUEST_MAPPING_DELIMITER = "/";
    static final String NAME_DELIMITER = "-";
    private static final String SUFFIX_HEADER = "Mock-Suffix";
    private static final String SUFFIX_DELIMITER = "--";
    private static final String VARIABLE_HEADER = "Mock-Variable";
    private static final String HEADER_SPLIT = "/";

    private HttpServletRequest request;
    private String endpoint;
    private String encodedEndpoint;
    private String body;

    public AbstractRequestFacade(HttpServletRequest request) {
        this.request = request;
        endpoint = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        encodedEndpoint = encodeEndpoint(endpoint);
        try {
            body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (Exception e) {
            // Body processed elsewhere. Do nothing.
        }
    }

    private String encodeEndpoint(String endpoint) {
        if (endpoint.startsWith(REQUEST_MAPPING_DELIMITER)) {
            endpoint = endpoint.substring(1);
        }
        String[] pathParts = endpoint.split(REQUEST_MAPPING_DELIMITER);
        endpoint = String.join(NAME_DELIMITER, pathParts);
        return endpoint.toLowerCase();
    }

    HttpServletRequest getRequest() {
        return request;
    }

    @Override
    public String getMethod() {
        return getRequest().getMethod().toLowerCase();
    }

    @Override
    public RequestMethod getRequestMethod() {
        return RequestMethod.valueOf(getRequest().getMethod());
    }

    @Override
    public String getEndpoint() {
        return endpoint;
    }

    String getEncodedEndpoint() {
        return encodedEndpoint;
    }

    String getBody() {
        return body;
    }

    @SuppressWarnings("unchecked")
    Map<String, String> getPathVariables() {
        Object pathVariables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (pathVariables instanceof Map) {
            return (Map<String, String>) pathVariables;
        }
        return new HashMap<>();
    }

    Map<String, String> getRequestParams() {
        Map<String, String> result = new HashMap<>();
        request.getParameterMap().forEach(
                (k, v) -> result.put(k, v[0])
        );
        return result;
    }

    private List<String[]> getHeadersParts(String headerName) {
        List<String[]> result = new ArrayList<>();
        Enumeration<String> headers = request.getHeaders(headerName);
        while (headers.hasMoreElements()) {
            String header = headers.nextElement();
            if (header != null && !header.isEmpty()) {
                result.add(header.trim().split(HEADER_SPLIT));
            }
        }
        return result;
    }

    Map<String, String> getHeaderVariables(String group) {
        Map<String, String> result = new HashMap<>();
        getHeadersParts(VARIABLE_HEADER).forEach(parts -> {
            if (parts.length == 3 && group.equalsIgnoreCase(parts[0])) {
                result.put(parts[1], parts[2]);
            } else if (parts.length > 3 && group.equalsIgnoreCase(parts[0]) && encodedEndpoint.equals(parts[1])) {
                result.put(parts[2], parts[3]);
            }
        });
        return result;
    }

    String getSuffix(String group) {
        for (String[] parts : getHeadersParts(SUFFIX_HEADER)) {
            if (parts.length == 2 && group.equalsIgnoreCase(parts[0])) {
                return SUFFIX_DELIMITER + parts[1];
            } else if (parts.length > 2 && group.equalsIgnoreCase(parts[0]) && encodedEndpoint.equals(parts[1])) {
                return SUFFIX_DELIMITER + parts[2];
            }
        }
        return "";
    }
}
