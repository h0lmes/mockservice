package com.mockachu.model;

import com.mockachu.template.MockVariables;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Map;

public class HttpRequestResult {

    private final boolean error;
    private final RequestMethod method;
    private final String uri;
    private final Map<String, List<String>> requestHeaders;
    private final HttpHeaders responseHeaders;
    private final String requestBody;
    private final String responseBody;
    private final MockVariables responseVariables;
    private final int statusCode;
    private final long duration;

    public HttpRequestResult(boolean error,
                             RequestMethod method,
                             String uri,
                             Map<String, List<String>> requestHeaders,
                             HttpHeaders responseHeaders,
                             String requestBody,
                             String responseBody,
                             MockVariables responseVariables,
                             int statusCode,
                             long startMillis) {
        this.error = error;
        this.method = method;
        this.uri = uri;
        this.requestHeaders = requestHeaders;
        this.responseHeaders = responseHeaders;
        this.requestBody = requestBody;
        this.responseBody = responseBody;
        this.responseVariables = responseVariables;
        this.statusCode = statusCode;
        this.duration = System.currentTimeMillis() - startMillis;
    }

    public boolean isError() {
        return error;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public MockVariables getResponseVariables() {
        return responseVariables;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public HttpHeaders getResponseHeaders() {
        return responseHeaders;
    }

    public RequestMethod getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public Map<String, List<String>> getRequestHeaders() {
        return requestHeaders;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(method.toString()).append(" ").append(uri).append('\n');
        if (requestHeaders != null && !requestHeaders.isEmpty()) {
            appendHeaders(builder);
        }
        if (!requestBody.isEmpty()) {
            builder.append(requestBody).append('\n');
        }

        if (error) {
            appendError(builder);
        }
        else {
            appendResult(builder);
        }
        return builder.toString();
    }

    private void appendHeaders(StringBuilder builder) {
        for (Map.Entry<String, List<String>> entry : requestHeaders.entrySet()) {
            for (String value : entry.getValue()) {
                builder.append(entry.getKey()).append(": ").append(value).append('\n');
            }
        }
        builder.append('\n');
    }

    private void appendError(StringBuilder builder) {
        builder.append("--- error in ").append(duration).append(" ms ---\n");
        builder.append(responseBody);
    }

    private void appendResult(StringBuilder builder) {
        builder.append("--- response in ").append(duration).append(" ms ---\n");
        var status = HttpStatus.resolve(statusCode);
        builder.append(statusCode).append(" ")
                .append(status == null ? "Unknown" : status.getReasonPhrase());
        if (responseBody != null && !responseBody.isBlank()) {
            builder.append('\n').append(responseBody);
        }
    }
}
