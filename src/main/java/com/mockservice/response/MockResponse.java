package com.mockservice.response;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public interface MockResponse {
    MockResponse setVariables(Map<String, String> variables, Map<String, Function<String[], String>> functions);

    int getResponseCode();
    String getResponseBody();
    HttpHeaders getResponseHeaders();
    void ifHasRequest(Consumer<MockResponse> consumer);
    HttpMethod getRequestMethod();
    String getRequestUrl();
    HttpHeaders getRequestHeaders();
    String getRequestBody();
}
