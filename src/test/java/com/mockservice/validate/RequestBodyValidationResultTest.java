package com.mockservice.validate;

import com.mockservice.domain.Route;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RequestBodyValidationResultTest {

    @Test
    public void ifError_Success_NotCalled() {
        final int[] calls = new int[1];

        RequestBodyValidationResult.success(new Route())
                .ifError(map -> calls[0]++);

        assertEquals(0, calls[0]);
    }

    @Test
    public void ifError_Error_Called() {
        final int[] calls = new int[1];

        RequestBodyValidationResult.error(new Route(), new RuntimeException("message"))
                .ifError(map -> calls[0]++);

        assertEquals(1, calls[0]);
    }
}
