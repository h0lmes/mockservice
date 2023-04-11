package com.mockservice.response;

import com.mockservice.template.MockFunctions;
import com.mockservice.template.MockVariables;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.function.Consumer;

public interface MockResponse {
    MockResponse setVariables(MockVariables variables, MockFunctions functions);
    MockResponse addVariables(MockVariables variables);

    int getResponseCode();
    String getResponseBody();
    HttpHeaders getResponseHeaders();
    void ifHasRequest(Consumer<MockResponse> consumer);
    HttpMethod getRequestMethod();
    String getRequestUrl();
    HttpHeaders getRequestHeaders();
    String getRequestBody();
}
