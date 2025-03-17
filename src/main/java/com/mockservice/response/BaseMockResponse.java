package com.mockservice.response;

import com.mockservice.template.MockFunctions;
import com.mockservice.template.MockVariables;
import com.mockservice.template.StringTemplate;
import org.springframework.http.HttpHeaders;

public class BaseMockResponse implements MockResponse {

    private final MockVariables variables = new MockVariables();
    private final MockFunctions functions = new MockFunctions();
    private final int responseCode;
    final HttpHeaders responseHeaders = new HttpHeaders();
    final StringTemplate responseBody;

    BaseMockResponse(int responseCode) {
        this.responseCode = responseCode;
        responseBody = new StringTemplate();
    }

    @Override
    public MockResponse setVariables(MockVariables variables, MockFunctions functions) {
        this.variables.clear();
        this.variables.putAll(variables);
        this.functions.clear();
        this.functions.putAll(functions);
        return this;
    }

    @Override
    public MockResponse addVariables(MockVariables variables) {
        this.variables.putAll(variables);
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
        return responseBody.toString(variables, functions);
    }
}
