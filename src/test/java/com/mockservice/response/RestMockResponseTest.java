package com.mockservice.response;

import com.mockservice.util.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RestMockResponseTest {

    private static final String RESPONSE_BODY = "{\"test\": \"test\"}";
    private static final int RESPONSE_CODE = 200;
    private static final String RESPONSE_HEADER = "Test";
    private static final String RESPONSE_HEADER_VALUE = "test";

    @Test
    public void parse_ValidResponse_ParsesSuccessfully() throws IOException {
        String json = IOUtils.asString("route_response_valid.json");
        MockResponse resource = new RestMockResponse(RESPONSE_CODE, json);

        assertEquals(RESPONSE_BODY, resource.getResponseBody());
        assertEquals(RESPONSE_CODE, resource.getResponseCode());
        List<String> headerValues = resource.getResponseHeaders().get(RESPONSE_HEADER);
        assertNotNull(headerValues);
        assertNotEquals(0, headerValues.size());
        assertEquals(RESPONSE_HEADER_VALUE, headerValues.get(0));
    }
}
