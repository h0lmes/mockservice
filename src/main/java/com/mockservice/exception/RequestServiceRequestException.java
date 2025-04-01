package com.mockservice.exception;

public class RequestServiceRequestException extends RuntimeException {
    private final int code;
    private final String body;

    public RequestServiceRequestException(int code, String body) {
        this.code = code;
        this.body = body;
    }

    public int getCode() {
        return code;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return body;
    }
}
