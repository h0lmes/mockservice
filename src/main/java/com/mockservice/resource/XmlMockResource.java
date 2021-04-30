package com.mockservice.resource;

import com.mockservice.template.StringTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;

import java.util.Map;

public class XmlMockResource implements MockResource {

    private HttpHeaders headers = new HttpHeaders();
    private StringTemplate body = new StringTemplate();

    public XmlMockResource(String resource) {
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
