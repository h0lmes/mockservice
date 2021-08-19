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

    @Test
    public void jsonToMap_NullString_ReturnsEmptyMap() throws IOException {
        Map<String, Object> objectMap = MapUtils.jsonToMap(null);
        assertTrue(objectMap.isEmpty());
    }

    @Test
    public void xmlToMap_ValidSoapEnvelope_MapAsExpected() throws IOException {
        String xml = IOUtils.asString("soap_envelope_valid.xml");
        Map<String, Object> objectMap = MapUtils.xmlToMap(xml);
        Map<String, String> map = MapUtils.flattenMap(objectMap);

        assertEquals("${enum:TEST1:TEST2}", map.get("NumberToDollarsResponse.NumberToDollarsResult"));
        assertEquals("${NumberToDollarsRequest.Value:DEFAULT_VALUE}", map.get("NumberToDollarsResponse.Result"));
    }

    @Test
    public void xmlToMap_EmptyString_ReturnsEmptyMap() {
        Map<String, Object> objectMap = MapUtils.xmlToMap("");
        assertTrue(objectMap.isEmpty());
    }

    @Test
    public void xmlToMap_NullString_ReturnsEmptyMap() {
        Map<String, Object> objectMap = MapUtils.xmlToMap(null);
        assertTrue(objectMap.isEmpty());
    }
}
