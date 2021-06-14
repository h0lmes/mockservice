package com.mockservice.response;

import com.mockservice.template.StringTemplate;
import com.mockservice.template.TemplateEngine;
import com.mockservice.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RestMockResponse implements MockResponse {

    private static final Logger log = LoggerFactory.getLogger(RestMockResponse.class);

    private static final String HTTP_1_1 = "HTTP/1.1";
    private static final String HTTP_HEADER_DELIMITER = ":";
    private static final int HTTP_HEADER_DELIMITER_LEN = HTTP_HEADER_DELIMITER.length();

    private Map<String, String> variables;
    private String host = "";
    private int responseCode = 200;
    private final HttpHeaders responseHeaders = new HttpHeaders();
    private final StringTemplate responseBody;
    private boolean containsRequest = false;
    private HttpMethod requestMethod = HttpMethod.GET;
    private String requestRelativeReference = "";
    private final HttpHeaders requestHeaders = new HttpHeaders();
    private final StringTemplate requestBody;

    public RestMockResponse(TemplateEngine engine, String resource) {
        responseBody = new StringTemplate(engine);
        requestBody = new StringTemplate(engine);
        responseHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
        requestHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
        read(resource);
    }

    private void read(String resource) {
        List<String> lines = IOUtil.toList(resource);
        List<String> responsePayload = new ArrayList<>();
        List<String> requestPayload = new ArrayList<>();
        boolean readingRequest = false;
        boolean readingHead = false;

        for (String line : lines) {
            if (isResponseStart(line)) {
                readingRequest = false;
                readingHead = true;
                setResponse(line);
            } else if (isRequestStart(line)) {
                readingRequest = true;
                readingHead = true;
                setRequest(line);
            } else if (readingHead && line.trim().isEmpty()) {
                readingHead = false;
            } else if (!readingRequest && readingHead) {
                addResponseHeader(line);
            } else if (readingRequest && readingHead) {
                addRequestHeader(line);
            } else if (!readingRequest) {
                responsePayload.add(line);
            } else {
                requestPayload.add(line);
            }
        }

        responsePayload.forEach(responseBody::add);
        requestPayload.forEach(requestBody::add);
    }

    private boolean isResponseStart(String line) {
        return line.startsWith(HTTP_1_1);
    }

    private boolean isRequestStart(String line) {
        return line.endsWith(HTTP_1_1);
    }

    private void setResponse(String line) {
        try {
            String[] split = line.split("\\s+");
            responseCode = Integer.parseInt(split[1]);
        } catch (NumberFormatException e) {
            log.warn("NaN response HTTP code: {}", line);
        }
    }

    private void setRequest(String line) {
        try {
            String methodAndRef = line.substring(0, line.indexOf(HTTP_1_1));
            String methodStr = methodAndRef.substring(0, methodAndRef.indexOf(' '));
            requestRelativeReference = methodAndRef.substring(methodAndRef.indexOf(' ')).trim();
            requestMethod = HttpMethod.valueOf(methodStr);
            containsRequest = true;
        } catch (Exception e) {
            log.warn("Invalid request starting line: {}", line);
        }
    }

    private void addResponseHeader(String line) {
        int delimiter = line.indexOf(HTTP_HEADER_DELIMITER);
        String key = line.substring(0, delimiter);
        String value = line.substring(delimiter + HTTP_HEADER_DELIMITER_LEN).trim();
        responseHeaders.add(key, value);
    }

    private void addRequestHeader(String line) {
        int delimiter = line.indexOf(HTTP_HEADER_DELIMITER);
        String key = line.substring(0, delimiter);
        String value = line.substring(delimiter + HTTP_HEADER_DELIMITER_LEN).trim();
        requestHeaders.add(key, value);
    }

    @Override
    public MockResponse setVariables(@Nullable Map<String, String> variables) {
        this.variables = variables;
        return this;
    }

    @Override
    public MockResponse setHost(String host) {
        this.host = host;
        return this;
    }

    @Override
    public int getResponseCode() {
        return responseCode;
    }

    @Override
    public HttpHeaders getResponseHeaders() {
        return responseHeaders;
    }

    @Override
    public String getResponseBody() {
        return responseBody.toString(variables);
    }

    @Override
    public boolean hasRequest() {
        return containsRequest;
    }

    @Override
    public HttpMethod getRequestMethod() {
        return requestMethod;
    }

    @Override
    public String getRequestRelativeReference() {
        return requestRelativeReference;
    }

    @Override
    public HttpHeaders getRequestHeaders() {
        return requestHeaders;
    }

    @Override
    public String getRequestBody() {
        return requestBody.toString(variables);
    }
}
