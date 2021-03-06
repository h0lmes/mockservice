package com.mockservice.response;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;

import java.util.Map;

public interface MockResponse {
    MockResponse setVariables(@Nullable Map<String, String> variables);
    String getResponseBody();
    int getResponseCode();
    HttpHeaders getResponseHeaders();
    boolean hasRequest();
    HttpMethod getRequestMethod();
    String getRequestUrl();
    HttpHeaders getRequestHeaders();
    String getRequestBody();
}
