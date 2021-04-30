package com.mockservice.request;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.mockservice.util.ResourceReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;


public class JsonHttpRequestFacadeTest {

    @Test
    public void jsonToFlatMapTest() throws JsonProcessingException {
        String json = ResourceReader.asString("classpath:map.json");
        Map<String, Object> objectMap = JsonHttpRequestFacade.jsonToMap(json);
        Map<String, String> map = JsonHttpRequestFacade.flattenMap(objectMap);

        Assertions.assertEquals(2, objectMap.size());
        Assertions.assertEquals(5, map.size());
        Assertions.assertEquals("value 1", map.get("key1"));
        Assertions.assertEquals("2021-04-19", map.get("key2.key1"));
        Assertions.assertEquals("10101", map.get("key2.key2.key1"));
        Assertions.assertEquals("null", map.get("key2.key2.key2"));
        Assertions.assertEquals("[value 1, value 2]", map.get("key2.key2.key3"));
    }
}
