package com.mockachu.web.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockachu.template.MockVariables;
import com.mockachu.util.MapUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.UriUtils;

import java.util.*;
import java.util.stream.Collectors;

public class MockRequestFacade {

    private static final Logger log = LoggerFactory.getLogger(MockRequestFacade.class);

    private static final String REQUEST_MAPPING_DELIMITER = "/";
    private static final String NAME_DELIMITER = "-";
    private static final String ALT_HEADER = "Mock-Alt";
    private static final String VARIABLE_HEADER = "Mock-Variable";
    private static final String AUTH_HEADER = "Authorization";
    private static final String HEADER_SPLIT = "/";

    private final ObjectMapper jsonMapper;
    private final String uri;
    private final String queryString;
    private final String endpoint;
    private final String encodedEndpoint;
    private final String requestMethod;
    private final MockVariables pathVariables = new MockVariables();
    private final MockVariables requestParams = new MockVariables();
    private final Map<String, List<String>> headers = new HashMap<>();
    private final List<String[]> mockVarHeaders;
    private final List<String[]> mockAltHeaders;
    private final List<String[]> authHeaders;
    private String body = "";

    @SuppressWarnings("unchecked")
    public MockRequestFacade(HttpServletRequest request, ObjectMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
        uri = request.getRequestURI() == null ? "" : UriUtils.decode(request.getRequestURI(), "UTF-8");
        queryString = request.getQueryString() == null ? null : UriUtils.decode(request.getQueryString(), "UTF-8");
        String pattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        endpoint = pattern == null || "/**".equals(pattern) ? uri : pattern;
        encodedEndpoint = encodeEndpoint(endpoint);
        requestMethod = request.getMethod();

        Object o = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (o instanceof Map) {
            pathVariables.putAll((Map<String, String>) o);
        }

        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap != null) {
            parameterMap.forEach((k, v) -> requestParams.put(k, v[0]));
        }

        mockVarHeaders = getHeadersParts(request, VARIABLE_HEADER);
        mockAltHeaders = getHeadersParts(request, ALT_HEADER);
        authHeaders = getHeadersParts(request, AUTH_HEADER);
        readHeaders(request);

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
        var headersEnumeration = request.getHeaders(headerName);
        if (headersEnumeration != null) {
            while (headersEnumeration.hasMoreElements()) {
                String header = headersEnumeration.nextElement();
                if (header != null && !header.isEmpty()) {
                    result.add(header.trim().split(HEADER_SPLIT));
                }
            }
        }
        return result;
    }

    private void readHeaders(HttpServletRequest request) {
        var names = request.getHeaderNames();
        if (names == null) return;
        while (names.hasMoreElements()) {
            String headerName = names.nextElement();
            var headersEnumeration = request.getHeaders(headerName);
            if (headersEnumeration != null) {
                var list = headers.computeIfAbsent(headerName, name -> new ArrayList<>());
                while (headersEnumeration.hasMoreElements()) {
                    list.add(headersEnumeration.nextElement());
                }
            }
        }
    }

    public RequestMethod getMethod() {
        return RequestMethod.valueOf(requestMethod);
    }

    public String getUri() {
        return uri;
    }

    public String getQueryString() {
        return queryString;
    }

    public String getUriAndQueryString() {
        if (queryString == null || queryString.isBlank()) {
            return uri;
        }
        return uri + "?" + queryString;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public Optional<String> getAlt() {
        for (String[] parts : mockAltHeaders) {
            if (parts.length > 1 && encodedEndpoint.equalsIgnoreCase(parts[0])) {
                return Optional.of(parts[1]);
            }
        }
        return Optional.empty();
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    private MockVariables getPathVariables() {
        return pathVariables;
    }

    private MockVariables getRequestParams() {
        return requestParams;
    }

    private MockVariables getHeaderVariables() {
        MockVariables result = new MockVariables();
        mockVarHeaders.forEach(parts -> {
            if (parts.length > 2 && encodedEndpoint.equalsIgnoreCase(parts[0])) {
                result.put(parts[1], parts[2]);
            }
        });
        return result;
    }

    public MockVariables getVariables(@Nullable MockVariables baseVariables, boolean isXmlBody) {
        MockVariables vars = new MockVariables();
        if (baseVariables != null) vars.putAll(baseVariables);
        getAuthorizationAsVariables().ifPresent(vars::putAll);
        if (isXmlBody) {
            putXmlBodyAsVariables(vars);
        } else {
            putJsonBodyAsVariables(vars);
        }
        vars.putAll(getPathVariables());
        vars.putAll(getRequestParams());
        vars.putAll(getHeaderVariables());
        return vars;
    }

    private void putJsonBodyAsVariables(MockVariables vars) {
        try {
            vars.putAll(MapUtils.flattenMap(MapUtils.toMap(body, jsonMapper)));
        } catch (JsonProcessingException e) {
            log.warn("Invalid JSON body:\n{}", body);
        }
    }

    private void putXmlBodyAsVariables(MockVariables vars) {
        try {
            vars.putAll(MapUtils.flattenMap(MapUtils.xmlToMap(body)));
        } catch (Exception e) {
            log.warn("Invalid XML body:\n{}", body);
        }
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
                        vars.putAll(MapUtils.flattenMap(MapUtils.toMap(payload, jsonMapper)));
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
