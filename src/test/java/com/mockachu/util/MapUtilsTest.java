package com.mockachu.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MapUtilsTest {

    private static ObjectMapper mapper;

    @BeforeAll
    private static void setupAll() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    @AfterAll
    private static void tearDownAll() {
        mapper = null;
    }

    @Test
    void jsonTotMapAndFlattenMap_ValidJson_MapAsExpected() throws IOException {
        String json = IOUtils.asString("map.json");
        Map<String, Object> objectMap = MapUtils.toMap(json, mapper);
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
    void jsonToMap_EmptyString_ReturnsEmptyMap() throws IOException {
        assertNull(MapUtils.<Map<String, Object>>toMap("", mapper));
    }

    @Test
    void jsonToMap_NullString_ReturnsEmptyMap() throws IOException {
        assertNull(MapUtils.<Map<String, Object>>toMap(null, mapper));
    }

    @Test
    void xmlToMap_ValidSoapEnvelope_MapAsExpected() throws IOException {
        String xml = IOUtils.asString("soap_envelope_valid.xml");
        Map<String, Object> objectMap = MapUtils.xmlToMap(xml);
        Map<String, String> map = MapUtils.flattenMap(objectMap);

        assertEquals("${enum:TEST1:TEST2}", map.get("NumberToDollarsResponse.NumberToDollarsResult"));
        assertEquals("${NumberToDollarsRequest.Value:DEFAULT_VALUE}", map.get("NumberToDollarsResponse.Result"));
    }

    @Test
    void xmlToMap_SoapEnvelopeNoBody_MapAsExpected() throws IOException {
        String xml = IOUtils.asString("soap_envelope_no_body.xml");
        Map<String, Object> objectMap = MapUtils.xmlToMap(xml);
        Map<String, String> map = MapUtils.flattenMap(objectMap);

        assertNotNull(map);
        assertTrue(map.isEmpty());
    }

    @Test
    void xmlToMap_EmptyString_ReturnsEmptyMap() {
        Map<String, Object> objectMap = MapUtils.xmlToMap("");
        assertTrue(objectMap.isEmpty());
    }

    @Test
    void xmlToMap_NullString_ReturnsEmptyMap() {
        Map<String, Object> objectMap = MapUtils.xmlToMap(null);
        assertTrue(objectMap.isEmpty());
    }
}
