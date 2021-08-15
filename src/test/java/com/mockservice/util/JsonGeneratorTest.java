package com.mockservice.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JsonGeneratorTest {

    private static final String NULL = "null";

    @Test
    public void generate_WithoutParameters_GeneratesRandomJson() {
        String json = JsonGenerator.generate();

        assertFalse(json.isEmpty());
    }

    @Test
    public void generate_Array_GeneratesArray() {
        String json = JsonGenerator.generate(JsonGenerator.JsonValueType.ARRAY);

        assertFalse(json.isEmpty());
        assertTrue(json.equals(NULL)
                || (json.startsWith("[") && json.endsWith("]"))
        );
    }

    @Test
    public void generate_Object_GeneratesObject() {
        String json = JsonGenerator.generate(JsonGenerator.JsonValueType.OBJECT);

        assertFalse(json.isEmpty());
        assertTrue(json.equals(NULL)
                || (json.startsWith("{") && json.endsWith("}"))
        );
    }

    @Test
    public void generate_Boolean_GeneratesBoolean() {
        String json = JsonGenerator.generate(JsonGenerator.JsonValueType.BOOLEAN);

        assertFalse(json.isEmpty());
        assertTrue(json.equals("true") || json.equals("false"));
    }

    @Test
    public void generate_Integer_GeneratesInteger() {
        String json = JsonGenerator.generate(JsonGenerator.JsonValueType.INTEGER);

        assertFalse(json.isEmpty());
        assertDoesNotThrow(() -> Integer.parseInt(json));
    }

    @Test
    public void generate_Number_GeneratesNumber() {
        String json = JsonGenerator.generate(JsonGenerator.JsonValueType.NUMBER);

        assertFalse(json.isEmpty());
        assertDoesNotThrow(() -> Double.parseDouble(json));
    }

    @Test
    public void generate_String_GeneratesString() {
        String json = JsonGenerator.generate(JsonGenerator.JsonValueType.STRING);

        assertFalse(json.isEmpty());
    }

    @Test
    public void generate_Null_GeneratesNull() {
        String json = JsonGenerator.generate(JsonGenerator.JsonValueType.NULL);

        assertEquals(NULL, json);
    }
}
