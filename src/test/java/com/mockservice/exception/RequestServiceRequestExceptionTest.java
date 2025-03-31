package com.mockservice.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestServiceRequestExceptionTest {

    @Test
    void test200() {
        var e = new RequestServiceRequestException(200, "body");
        assertEquals(200, e.getCode());
        assertEquals("body", e.getBody());
        assertTrue(e.toString().contains("200"));
        assertTrue(e.toString().contains("body"));
    }

    @Test
    void testUnknown() {
        var e = new RequestServiceRequestException(900, "body");
        assertEquals(900, e.getCode());
        assertEquals("body", e.getBody());
        assertTrue(e.toString().contains("900"));
        assertTrue(e.toString().contains("Unknown"));
    }
}
