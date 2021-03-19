package com.mockservice.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResourceWrapperTest {

    private static final Integer TEST_HTTP_CODE = 201;
    private static final String TEST_BODY = "{\"test\": \"test\"}";
    private static final String TEST_RESOURCE_STRING = "HTTP/1.1 " + TEST_HTTP_CODE + "\n\n" + TEST_BODY;

    @Test
    public void parserTest() {
        ResourceWrapper wrapper = new ResourceWrapper(TEST_RESOURCE_STRING);

        assertEquals(TEST_HTTP_CODE.intValue(), wrapper.getCode());
        assertEquals(TEST_BODY, wrapper.getBody());
    }
}
