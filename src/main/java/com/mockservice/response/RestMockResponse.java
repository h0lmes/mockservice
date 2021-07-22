package com.mockservice.response;

import com.mockservice.template.StringTemplate;
import com.mockservice.template.TemplateEngine;
import com.mockservice.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class RestMockResponse implements MockResponse {

    private static final Logger log = LoggerFactory.getLogger(RestMockResponse.class);

    private static final String HTTP_1_1 = "HTTP/1.1";
    private static final String DELIMITER_STRING = "---";
    private static final String HTTP_HEADER_DELIMITER = ":";
    private static final int HTTP_HEADER_DELIMITER_LEN = HTTP_HEADER_DELIMITER.length();

    private Map<String, String> variables;
    private final HttpHeaders responseHeaders = new HttpHeaders();
    private final StringTemplate responseBody;
    private boolean containsRequest = false;
    private HttpMethod requestMethod = HttpMethod.GET;
    private final StringTemplate requestUrl;
    private final HttpHeaders requestHeaders = new HttpHeaders();
    private final StringTemplate requestBody;

    public RestMockResponse(TemplateEngine engine, String resource) {
        responseBody = new StringTemplate(engine);
        requestBody = new StringTemplate(engine);
        requestUrl = new StringTemplate(engine);
        responseHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
        requestHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
        read(resource);
    }

    private void read(String resource) {
        List<String> lines = IOUtil.toList(resource);
        boolean readingRequest = false;
        boolean readingHead = false;

        for (String line : lines) {
            if (isDelimiter(line)) {
                // ignore
            } else if (isResponseStart(line)) {
                readingRequest = false;
                readingHead = true;
            } else if (isRequestStart(line)) {
                readingRequest = true;
                readingHead = true;
                setRequest(line);
            } else if (readingHead && line.trim().isEmpty()) {
                readingHead = false;
            } else if (!readingRequest && readingHead) {
                addHeader(line, responseHeaders);
            } else if (readingRequest && readingHead) {
                addHeader(line, requestHeaders);
            } else if (!line.isEmpty()) {
                if (readingRequest) {
                    requestBody.add(line);
                } else {
                    responseBody.add(line);
                }
            }
        }
    }

    private boolean isRequestStart(String line) {
        return line.trim().endsWith(HTTP_1_1);
    }

    private boolean isResponseStart(String line) {
        return line.trim().startsWith(HTTP_1_1);
    }

    private boolean isDelimiter(String line) {
        return line.trim().startsWith(DELIMITER_STRING);
    }

    private void setRequest(String line) {
        try {
            String methodAndUrl = line.substring(0, line.indexOf(HTTP_1_1)).trim();
            String methodStr = methodAndUrl.substring(0, methodAndUrl.indexOf(' '));
            requestUrl.add(methodAndUrl.substring(methodAndUrl.indexOf(' ')).trim());
            requestMethod = HttpMethod.valueOf(methodStr);
            containsRequest = true;
        } catch (Exception e) {
            log.warn("Invalid request first line: {}", line);
        }
    }

    private void addHeader(String line, HttpHeaders toHeaders) {
        int delimiter = line.indexOf(HTTP_HEADER_DELIMITER);
        String key = line.substring(0, delimiter);
        String value = line.substring(delimiter + HTTP_HEADER_DELIMITER_LEN).trim();
        toHeaders.add(key, value);
    }

    @Override
    public MockResponse setVariables(@Nullable Map<String, String> variables) {
        this.variables = variables;
        return this;
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
    public void ifHasRequest(Consumer<MockResponse> consumer) {
        if (containsRequest) {
            consumer.accept(this);
        }
    }

    @Override
    public HttpMethod getRequestMethod() {
        return requestMethod;
    }

    @Override
    public String getRequestUrl() {
        return requestUrl.toString(variables);
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
