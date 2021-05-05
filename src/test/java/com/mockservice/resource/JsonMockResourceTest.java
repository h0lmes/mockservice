package com.mockservice.resource;

import com.mockservice.util.ResourceReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonMockResourceTest {

    private static final Integer TEST_HTTP_CODE = 201;
    private static final String TEST_BODY = "{\"test\": \"test\"}";

    @Test
    public void parserTest() throws IOException {
        String json = ResourceReader.asString("classpath:resource.json");
        MockResource resource = new JsonMockResource(json);

        assertEquals(TEST_HTTP_CODE.intValue(), resource.getCode());
        assertEquals(TEST_BODY, resource.getBody(null));
    }
}
