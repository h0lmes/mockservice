package com.mockachu.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RequestServiceRequestExceptionTest {

    @Test
    void test() {
        var e = new RequestServiceRequestException(200, "body");
        assertEquals(200, e.getCode());
        assertEquals("body", e.getBody());
        assertEquals("body", e.toString());
    }
}
