package com.mockachu.exception;

import org.springframework.http.HttpHeaders;

public class RequestServiceRequestException extends RuntimeException {
    private final int code;
    private final String body;
    private final HttpHeaders headers;

    public RequestServiceRequestException(int code, String body, HttpHeaders headers) {
        this.code = code;
        this.body = body;
        this.headers = headers;
    }

    public int getCode() {
        return code;
    }

    public String getBody() {
        return body;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    @Override
    public String toString() {
        return body;
    }
}
