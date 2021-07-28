package com.mockservice.response;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;

import java.util.Map;
import java.util.function.Consumer;

public interface MockResponse {
    MockResponse putVariables(@Nullable Map<String, String> variables);
    String getResponseBody();
    HttpHeaders getResponseHeaders();
    void ifHasRequest(Consumer<MockResponse> consumer);
    HttpMethod getRequestMethod();
    String getRequestUrl();
    HttpHeaders getRequestHeaders();
    String getRequestBody();
}
