package com.mockservice.resource;

import com.mockservice.template.StringTemplate;
import com.mockservice.template.TemplateEngine;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;

import java.util.Map;

public class SoapMockResource implements MockResource {

    private final HttpHeaders headers = new HttpHeaders();
    private final StringTemplate body;

    public SoapMockResource(TemplateEngine engine, String resource) {
        body = new StringTemplate(engine);
        body.add(resource);
        headers.add(HttpHeaders.CONTENT_TYPE, "text/xml;charset=UTF-8");
    }

    @Override
    public String getBody(@Nullable Map<String, String> variables) {
        return body.toString(variables);
    }

    @Override
    public int getCode() {
        return 200;
    }

    @Override
    public HttpHeaders getHeaders() {
        return headers;
    }
}
