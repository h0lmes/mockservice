package com.mockservice.response;

import org.springframework.http.HttpHeaders;

public class SoapMockResponse extends BaseMockResponse {

    public SoapMockResponse(String response) {
        super();
        responseBody.add(response);
        responseHeaders.add(HttpHeaders.CONTENT_TYPE, "text/xml;charset=UTF-8");
    }
}
