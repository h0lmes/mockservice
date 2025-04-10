package com.mockachu.response;

import com.mockachu.template.MockFunctions;
import com.mockachu.template.MockVariables;
import com.mockachu.template.StringTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

public class BaseMockResponse implements MockResponse {

    private final MockVariables variables = new MockVariables();
    private MockFunctions functions;
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
        this.functions = functions;
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

    public ResponseEntity<String> asResponseEntity() {
        return ResponseEntity
                .status(getResponseCode())
                .headers(getResponseHeaders())
                .body(getResponseBody());
    }
}
