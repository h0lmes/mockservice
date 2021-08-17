package com.mockservice.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractRequestFacade implements RequestFacade {

    private static final Logger log = LoggerFactory.getLogger(AbstractRequestFacade.class);

    private static final String REQUEST_MAPPING_DELIMITER = "/";
    private static final String NAME_DELIMITER = "-";
    private static final String ALT_HEADER = "Mock-Alt";
    private static final String VARIABLE_HEADER = "Mock-Variable";
    private static final String AUTH_HEADER = "Authorization";
    private static final String HEADER_SPLIT = "/";

    final ObjectMapper jsonMapper;
    private final String endpoint;
    private final String encodedEndpoint;
    private final String requestMethod;
    private final Map<String, String> pathVariables;
    private final Map<String, String> requestParams = new HashMap<>();
    private final List<String[]> mockVarHeaders;
    private final List<String[]> mockAltHeaders;
    final List<String[]> authHeaders;
    private String body = "";

    @SuppressWarnings("unchecked")
    public AbstractRequestFacade(HttpServletRequest request, ObjectMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
        endpoint = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE) == null
                ? "" : (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        encodedEndpoint = encodeEndpoint(endpoint);
        requestMethod = request.getMethod();

        Object o = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (o instanceof Map) {
            pathVariables = (Map<String, String>) o;
        } else {
            pathVariables = new HashMap<>();
        }

        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap != null) {
            parameterMap.forEach((k, v) -> requestParams.put(k, v[0]));
        }

        mockVarHeaders = getHeadersParts(request, VARIABLE_HEADER);
        mockAltHeaders = getHeadersParts(request, ALT_HEADER);
        authHeaders = getHeadersParts(request, AUTH_HEADER);

        try {
            body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (Exception e) {
            log.warn("Request body processed elsewhere");
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

    private List<String[]> getHeadersParts(HttpServletRequest request, String headerName) {
        List<String[]> result = new ArrayList<>();
        Enumeration<String> headers = request.getHeaders(headerName);
        if (headers != null) {
            while (headers.hasMoreElements()) {
                String header = headers.nextElement();
                if (header != null && !header.isEmpty()) {
                    result.add(header.trim().split(HEADER_SPLIT));
                }
            }
        }
        return result;
    }

    @Override
    public RequestMethod getRequestMethod() {
        return RequestMethod.valueOf(requestMethod);
    }

    @Override
    public String getEndpoint() {
        return endpoint;
    }

    @Override
    public Optional<String> getAlt() {
        for (String[] parts : mockAltHeaders) {
            if (parts.length > 1 && encodedEndpoint.equalsIgnoreCase(parts[0])) {
                return Optional.of(parts[1]);
            }
        }
        return Optional.empty();
    }

    @Override
    public String getBody() {
        return body;
    }

    Map<String, String> getPathVariables() {
        return pathVariables;
    }

    Map<String, String> getRequestParams() {
        return requestParams;
    }

    Map<String, String> getHeaderVariables() {
        Map<String, String> result = new HashMap<>();
        mockVarHeaders.forEach(parts -> {
            if (parts.length > 2 && encodedEndpoint.equalsIgnoreCase(parts[0])) {
                result.put(parts[1], parts[2]);
            }
        });
        return result;
    }
}
