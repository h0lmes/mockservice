package com.mockservice.response;

import com.mockservice.template.StringTemplate;
import com.mockservice.template.TemplateEngine;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SoapMockResponse implements MockResponse {

    private Map<String, String> variables = new HashMap<>();
    private final HttpHeaders headers = new HttpHeaders();
    private final StringTemplate body;

    public SoapMockResponse(TemplateEngine engine, String response) {
        body = new StringTemplate(engine);
        body.add(response);
        headers.add(HttpHeaders.CONTENT_TYPE, "text/xml;charset=UTF-8");
    }

    @Override
    public MockResponse setVariables(@Nullable Map<String, String> variables) {
        this.variables.clear();
        this.variables.putAll(variables);
        return this;
    }

    @Override
    public HttpHeaders getResponseHeaders() {
        return headers;
    }

    @Override
    public String getResponseBody() {
        return body.toString(variables);
    }

    @Override
    public void ifHasRequest(Consumer<MockResponse> consumer) {
        //
    }

    @Override
    public HttpMethod getRequestMethod() {
        return null;
    }

    @Override
    public String getRequestUrl() {
        return null;
    }

    @Override
    public HttpHeaders getRequestHeaders() {
        return null;
    }

    @Override
    public String getRequestBody() {
        return null;
    }
}
