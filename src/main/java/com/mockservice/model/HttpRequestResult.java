package com.mockservice.model;

import com.mockservice.template.MockVariables;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Map;

public class HttpRequestResult {

    private final boolean failed;
    private final RequestMethod method;
    private final String uri;
    private final Map<String, List<String>> headers;
    private final String requestBody;
    private final String responseBody;
    private final MockVariables responseVariables;
    private final int statusCode;
    private final long duration;

    public HttpRequestResult(boolean failed,
                             RequestMethod method,
                             String uri,
                             Map<String, List<String>> headers,
                             String requestBody,
                             String responseBody,
                             MockVariables responseVariables,
                             int statusCode,
                             long startMillis) {
        this.failed = failed;
        this.method = method;
        this.uri = uri;
        this.headers = headers;
        this.requestBody = requestBody;
        this.responseBody = responseBody;
        this.responseVariables = responseVariables;
        this.statusCode = statusCode;
        this.duration = System.currentTimeMillis() - startMillis;
    }

    public boolean isFailed() {
        return failed;
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(method.toString()).append(" ").append(uri).append('\n');
        if (headers != null && !headers.isEmpty()) {
            appendHeaders(builder);
        }
        if (!requestBody.isEmpty()) {
            builder.append('\n').append(requestBody).append('\n');
        }

        if (failed) {
            appendError(builder);
        }
        else {
            appendResult(builder);
        }
        return builder.toString();
    }

    private void appendHeaders(StringBuilder builder) {
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
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
                .append(status == null ? "Unknown" : status.getReasonPhrase()).append('\n');
        builder.append(responseBody);
    }
}
