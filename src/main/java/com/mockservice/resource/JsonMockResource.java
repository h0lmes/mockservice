package com.mockservice.resource;

import org.springframework.http.HttpHeaders;

public class JsonMockResource extends AbstractMockResource {

    public JsonMockResource(String resource) {
        super(resource);
        getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
    }
}
