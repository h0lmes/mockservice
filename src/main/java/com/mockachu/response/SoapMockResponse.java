package com.mockachu.response;

import org.springframework.http.HttpHeaders;

public class SoapMockResponse extends BaseMockResponse {

    public SoapMockResponse(int responseCode, String response) {
        super(responseCode);
        responseBody.add(response);
        responseHeaders.add(HttpHeaders.CONTENT_TYPE, "text/xml;charset=UTF-8");
    }
}
