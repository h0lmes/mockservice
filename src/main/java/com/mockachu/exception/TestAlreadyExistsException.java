package com.mockachu.exception;

import com.mockachu.domain.ApiTest;

public class TestAlreadyExistsException extends RuntimeException {

    private final transient ApiTest apiTest;

    public TestAlreadyExistsException(ApiTest apiTest) {
        this.apiTest = apiTest;
    }

    @Override
    public String getMessage() {
        return "Test already exists: " + apiTest;
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
