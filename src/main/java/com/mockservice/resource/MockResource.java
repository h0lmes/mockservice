package com.mockservice.resource;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;

import java.util.Map;
import java.util.function.Function;

public interface MockResource {
    String getBody(@Nullable Map<String, String> variables);

    int getCode();

    HttpHeaders getHeaders();
}
