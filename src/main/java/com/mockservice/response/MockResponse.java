package com.mockservice.response;

import com.mockservice.template.MockFunctions;
import com.mockservice.template.MockVariables;
import org.springframework.http.HttpHeaders;

public interface MockResponse {
    MockResponse setVariables(MockVariables variables, MockFunctions functions);
    MockResponse addVariables(MockVariables variables);
    int getResponseCode();
    String getResponseBody();
    HttpHeaders getResponseHeaders();
}
