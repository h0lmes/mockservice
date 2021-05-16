package com.mockservice.resource;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;

import java.util.Map;

public interface MockResource {
    String getBody(@Nullable Map<String, String> variables);
    int getCode();
    HttpHeaders getHeaders();
}
