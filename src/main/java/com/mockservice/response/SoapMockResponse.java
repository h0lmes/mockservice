package com.mockservice.response;

import com.mockservice.template.StringTemplate;
import com.mockservice.template.TemplateEngine;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;

import java.util.Map;

public class SoapMockResponse implements MockResponse {

    private Map<String, String> variables;
    private String host = "";
    private final HttpHeaders headers = new HttpHeaders();
    private final StringTemplate body;

    public SoapMockResponse(TemplateEngine engine, String resource) {
        body = new StringTemplate(engine);
        body.add(resource);
        headers.add(HttpHeaders.CONTENT_TYPE, "text/xml;charset=UTF-8");
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
        return 200;
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
    public boolean hasRequest() {
        return false;
    }

    @Override
    public HttpMethod getRequestMethod() {
        return null;
    }

    @Override
    public String getRequestRelativeReference() {
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
