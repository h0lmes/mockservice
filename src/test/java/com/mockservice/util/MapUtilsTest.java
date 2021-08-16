package com.mockservice.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MapUtilsTest {

    @Test
    public void jsonTotMapAndFlattenMap_ValidJson_MapAsExpected() throws IOException {
        String json = IOUtils.asString("map.json");
        Map<String, Object> objectMap = MapUtils.jsonToMap(json);
        Map<String, String> map = MapUtils.flattenMap(objectMap);

        assertEquals(2, objectMap.size());
        assertEquals(5, map.size());
        assertEquals("value 1", map.get("key1"));
        assertEquals("2021-04-19", map.get("key2.key1"));
        assertEquals("10101", map.get("key2.key2.key1"));
        assertNull(map.get("key2.key2.key2"));
        assertEquals("[value 1, value 2]", map.get("key2.key2.key3"));
    }

    @Test
    public void jsonToMap_EmptyString_ReturnsEmptyMap() throws IOException {
        Map<String, Object> objectMap = MapUtils.jsonToMap("");
        assertTrue(objectMap.isEmpty());
    }
}
