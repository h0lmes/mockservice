package com.mockservice.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

public class MapUtilsTest {

    @Test
    public void jsonToFlatMapTest() throws IOException {
        String json = IOUtil.asString("map.json");
        Map<String, Object> objectMap = MapUtils.jsonToMap(json);
        Map<String, String> map = MapUtils.flattenMap(objectMap);

        Assertions.assertEquals(2, objectMap.size());
        Assertions.assertEquals(5, map.size());
        Assertions.assertEquals("value 1", map.get("key1"));
        Assertions.assertEquals("2021-04-19", map.get("key2.key1"));
        Assertions.assertEquals("10101", map.get("key2.key2.key1"));
        Assertions.assertNull(map.get("key2.key2.key2"));
        Assertions.assertEquals("[value 1, value 2]", map.get("key2.key2.key3"));
    }
}
