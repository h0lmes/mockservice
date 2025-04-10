package com.mockachu.response;

import com.mockachu.template.MockFunctions;
import com.mockachu.template.MockVariables;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

public interface MockResponse {
    MockResponse setVariables(MockVariables variables, MockFunctions functions);
    MockResponse addVariables(MockVariables variables);
    int getResponseCode();
    String getResponseBody();
    HttpHeaders getResponseHeaders();
    ResponseEntity<String> asResponseEntity();
}
