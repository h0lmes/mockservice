package com.mockservice.response;

import com.mockservice.util.IOUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RestMockResponseTest {

    private static final Integer TEST_HTTP_CODE = 201;
    private static final String TEST_BODY = "{\"test\": \"test\"}";

    @Test
    public void parserTest() throws IOException {
        String json = IOUtil.asString("resource.json");
        MockResponse resource = new RestMockResponse(null, json);

        assertEquals(TEST_HTTP_CODE.intValue(), resource.getResponseCode());
        assertEquals(TEST_BODY, resource.getResponseBody());
    }
}
