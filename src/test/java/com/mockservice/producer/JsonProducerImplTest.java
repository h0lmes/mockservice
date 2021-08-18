package com.mockservice.producer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JsonProducerImplTest {

    private static final String NULL = "null";

    private JsonProducer producer() {
        return new JsonProducerImpl(new ValueProducerImpl());
    }

    @Test
    public void generate_WithoutParameters_GeneratesRandomJson() {
        String json = producer().generate();

        assertFalse(json.isEmpty());
    }

    @Test
    public void generate_Array_GeneratesArray() {
        String json = producer().generate(JsonValueType.ARRAY);

        assertFalse(json.isEmpty());
        assertTrue(json.equals(NULL)
                || (json.startsWith("[") && json.endsWith("]"))
        );
    }

    @Test
    public void generate_Object_GeneratesObject() {
        String json = producer().generate(JsonValueType.OBJECT);

        assertFalse(json.isEmpty());
        assertTrue(json.equals(NULL)
                || (json.startsWith("{") && json.endsWith("}"))
        );
    }

    @Test
    public void generate_Boolean_GeneratesBoolean() {
        String json = producer().generate(JsonValueType.BOOLEAN);

        assertFalse(json.isEmpty());
        assertDoesNotThrow(() -> Boolean.parseBoolean(json));
    }

    @Test
    public void generate_Integer_GeneratesInteger() {
        String json = producer().generate(JsonValueType.INTEGER);

        assertFalse(json.isEmpty());
        assertDoesNotThrow(() -> Integer.parseInt(json));
    }

    @Test
    public void generate_Number_GeneratesNumber() {
        String json = producer().generate(JsonValueType.NUMBER);

        assertFalse(json.isEmpty());
        assertDoesNotThrow(() -> Double.parseDouble(json));
    }

    @Test
    public void generate_String_GeneratesString() {
        String json = producer().generate(JsonValueType.STRING);

        assertFalse(json.isEmpty());
    }

    @Test
    public void generate_Null_GeneratesNull() {
        String json = producer().generate(JsonValueType.NULL);

        assertEquals(NULL, json);
    }
}
