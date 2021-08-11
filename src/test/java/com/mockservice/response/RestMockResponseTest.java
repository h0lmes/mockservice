package com.mockservice.response;

import com.mockservice.util.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RestMockResponseTest {

    private static final String TEST_BODY = "{\"test\": \"test\"}";

    @Test
    public void parserTest() throws IOException {
        String json = IOUtils.asString("resource.json");
        MockResponse resource = new RestMockResponse(200, json);

        assertEquals(TEST_BODY, resource.getResponseBody());
    }
}
