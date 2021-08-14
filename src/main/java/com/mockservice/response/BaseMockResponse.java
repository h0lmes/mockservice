package com.mockservice.response;

import com.mockservice.template.StringTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class BaseMockResponse implements MockResponse {

    private final Map<String, String> variables = new HashMap<>();
    private final Map<String, Function<String[], String>> functions = new HashMap<>();
    private final int responseCode;
    final HttpHeaders responseHeaders = new HttpHeaders();
    final StringTemplate responseBody;
    boolean containsRequest = false;
    HttpMethod requestMethod = HttpMethod.GET;
    final StringTemplate requestUrl;
    final HttpHeaders requestHeaders = new HttpHeaders();
    final StringTemplate requestBody;

    BaseMockResponse(int responseCode) {
        this.responseCode = responseCode;
        responseBody = new StringTemplate();
        requestBody = new StringTemplate();
        requestUrl = new StringTemplate();
    }

    @Override
    public MockResponse setVariables(Map<String, String> variables, Map<String, Function<String[], String>> functions) {
        this.variables.clear();
        this.variables.putAll(variables);
        this.functions.clear();
        this.functions.putAll(functions);
        return this;
    }

    @Override
    public MockResponse addVariables(Map<String, String> variables) {
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
        return requestUrl.toString(variables, functions);
    }

    @Override
    public HttpHeaders getRequestHeaders() {
        return requestHeaders;
    }

    @Override
    public String getRequestBody() {
        return requestBody.toString(variables, functions);
    }
}
