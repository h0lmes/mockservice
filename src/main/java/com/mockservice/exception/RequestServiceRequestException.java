package com.mockservice.exception;

import org.springframework.http.HttpStatus;

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
        var status = HttpStatus.resolve(code);
        var phrase = status == null ? "Unknown" : status.getReasonPhrase();
        return "HTTP " + code + " " + phrase + "\n" + body;
    }
}
