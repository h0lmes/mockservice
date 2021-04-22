package com.mockservice.request;


import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class HttpServletRequestFacadeTest {

    @Test
    public void jsonToFlatMapTest() throws JsonProcessingException {
        String json =
                "{" +
                    "\"key1\": \"value 1\", " +
                    "\"key2\": {" +
                        "\"key1\": \"2021-04-19\"," +
                        "\"key2\": {" +
                            "\"key1\": 10101, " +
                            "\"key2\": [" +
                                "\"value 1\", \"value 2\"" +
                            "]" +
                        "}" +
                    "}" +
                "}";

        Map<String, String> map = HttpServletRequestFacade.jsonToFlatMap(json);

        Assertions.assertEquals(4, map.size());
        Assertions.assertEquals("value 1", map.get("key1"));
        Assertions.assertEquals("2021-04-19", map.get("key2.key1"));
        Assertions.assertEquals("10101", map.get("key2.key2.key1"));
        Assertions.assertEquals("[value 1, value 2]", map.get("key2.key2.key2"));
    }
}
