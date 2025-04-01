package com.mockservice.model;

import com.mockservice.domain.OutboundRequest;
import com.mockservice.template.MockVariables;
import com.mockservice.template.RequestHeadersTemplate;
import org.springframework.http.HttpStatus;

public class OutboundRequestResult {

    private final boolean failed;
    private final OutboundRequest request;
    private final String uri;
    private final RequestHeadersTemplate headersTemplate;
    private final String requestBody;
    private final String responseBody;
    private final MockVariables responseVariables;
    private final int statusCode;
    private final long duration;

    public OutboundRequestResult(boolean failed,
                                 OutboundRequest request,
                                 String uri,
                                 RequestHeadersTemplate headersTemplate,
                                 String requestBody,
                                 String responseBody,
                                 MockVariables responseVariables,
                                 int statusCode,
                                 long startMillis) {
        this.failed = failed;
        this.request = request;
        this.uri = uri;
        this.headersTemplate = headersTemplate;
        this.requestBody = requestBody;
        this.responseBody = responseBody;
        this.responseVariables = responseVariables;
        this.statusCode = statusCode;
        this.duration = System.currentTimeMillis() - startMillis;
    }

    public boolean isFailed() {
        return failed;
    }

    public OutboundRequest getRequest() {
        return request;
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
        builder.append(request.getMethod().toString()).append(" ").append(uri).append('\n');
        if (!headersTemplate.isEmpty()) {
            builder.append(headersTemplate).append('\n');
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

    private void appendError(StringBuilder builder) {
        builder.append("--- error in ").append(duration).append(" ms ---\n");
        builder.append(responseBody);
    }

    private void appendResult(StringBuilder builder) {
        builder.append("--- response in ").append(duration).append(" ms ---\n");
        var status = HttpStatus.resolve(statusCode);
        builder.append("HTTP ")
                .append(statusCode)
                .append(" ")
                .append(status == null ? "Unknown" : status.getReasonPhrase())
                .append('\n');
        builder.append(responseBody);
    }
}
