package com.mockservice.response;

import com.mockservice.util.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RestMockResponseTest {

    private static final String RESPONSE_BODY = "{\"test\": \"test\"}";
    private static final int RESPONSE_CODE = 200;
    private static final String RESPONSE_HEADER = "Test";
    private static final String RESPONSE_HEADER_VALUE = "test";

    private static final String REQUEST_URL = "http://localhost:8081/api";
    private static final HttpMethod REQUEST_METHOD = HttpMethod.POST;
    private static final String REQUEST_BODY = "{\"id\": 42}";
    private static final String REQUEST_HEADER = "Test-Callback";
    private static final String REQUEST_HEADER_VALUE = "test callback";

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

        assertEquals(REQUEST_METHOD, resource.getRequestMethod());
        assertEquals(REQUEST_URL, resource.getRequestUrl());
        assertEquals(REQUEST_BODY, resource.getRequestBody());
        List<String> requestHeaderValues = resource.getRequestHeaders().get(REQUEST_HEADER);
        assertNotNull(requestHeaderValues);
        assertNotEquals(0, requestHeaderValues.size());
        assertEquals(REQUEST_HEADER_VALUE, requestHeaderValues.get(0));
    }

    @Test
    public void parse_InvalidResponse_ExceptionThrown() throws IOException {
        String json = IOUtils.asString("route_response_invalid_request.json");

        assertThrows(IllegalArgumentException.class, () -> new RestMockResponse(RESPONSE_CODE, json));
    }
}
