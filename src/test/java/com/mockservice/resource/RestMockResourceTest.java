package com.mockservice.resource;

import com.mockservice.util.FileReaderWriterUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RestMockResourceTest {

    private static final Integer TEST_HTTP_CODE = 201;
    private static final String TEST_BODY = "{\"test\": \"test\"}";

    @Test
    public void parserTest() throws IOException {
        String json = FileReaderWriterUtil.asString("resource.json");
        MockResource resource = new RestMockResource(null, json);

        assertEquals(TEST_HTTP_CODE.intValue(), resource.getCode());
        assertEquals(TEST_BODY, resource.getBody(null));
    }
}
