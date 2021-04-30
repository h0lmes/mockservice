package com.mockservice.resource;

import org.springframework.http.HttpHeaders;

public class XmlMockResource extends AbstractMockResource {

    public XmlMockResource(String resource) {
        super(resource);
        getHeaders().add(HttpHeaders.CONTENT_TYPE, "text/xml;charset=UTF-8");
    }
}
