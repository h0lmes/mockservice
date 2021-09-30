package com.mockservice.producer;

import com.mockservice.util.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class JsonProducerImplTest {

    private static final String NULL = "null";

    @Mock
    private RandomUtils randomUtils;

    private JsonProducer producer() {
        return new JsonProducerImpl(new ValueProducerImpl(randomUtils), randomUtils);
    }

    @Test
    public void generate_WithoutParameters_GeneratesRandomJson() {
        String json = producer().generate();

        assertFalse(json.isEmpty());
    }

    @Test
    public void generate_Array_GeneratesArray() {
        Mockito.when(randomUtils.rnd(anyInt())).thenReturn(1);
        Mockito.when(randomUtils.withChance(anyInt())).thenReturn(false);
        String json = producer().generate(JsonValueType.ARRAY);

        assertTrue(json.equals(NULL)
                || (json.startsWith("[") && json.endsWith("]"))
        );
    }

    @Test
    public void generate_Array2_GeneratesArray() {
        Mockito.when(randomUtils.withChance(anyInt())).thenReturn(true);
        String json = producer().generate(JsonValueType.ARRAY);

        assertTrue(json.equals(NULL)
                || (json.startsWith("[") && json.endsWith("]"))
        );
    }

    @Test
    public void generateArray_OfObject_GeneratesArray() {
        Mockito.when(randomUtils.rnd(anyInt())).thenReturn(1);
        Mockito.when(randomUtils.withChance(anyInt())).thenReturn(false);
        String json = producer().generateArray(0, 5, JsonValueType.OBJECT);

        assertTrue(json.equals(NULL)
                || (json.startsWith("[") && json.endsWith("]"))
        );
    }

    @Test
    public void generateArray_OfArray_GeneratesArray() {
        Mockito.when(randomUtils.rnd(anyInt())).thenReturn(1);
        Mockito.when(randomUtils.withChance(anyInt())).thenReturn(false);
        String json = producer().generateArray(0, 5, JsonValueType.ARRAY);

        assertTrue(json.equals(NULL)
                || (json.startsWith("[") && json.endsWith("]"))
        );
    }

    @Test
    public void generate_Object_GeneratesObject() {
        Mockito.when(randomUtils.rnd(anyInt())).thenReturn(1);
        Mockito.when(randomUtils.withChance(anyInt())).thenReturn(false);
        String json = producer().generate(JsonValueType.OBJECT);

        assertTrue(json.equals(NULL)
                || (json.startsWith("{") && json.endsWith("}"))
        );
    }

    @Test
    public void generate_Object2_GeneratesObject() {
        Mockito.when(randomUtils.withChance(anyInt())).thenReturn(true);
        String json = producer().generate(JsonValueType.OBJECT);

        assertTrue(json.equals(NULL)
                || (json.startsWith("{") && json.endsWith("}"))
        );
    }

    @Test
    public void generate_Boolean_GeneratesBoolean() {
        String json = producer().generate(JsonValueType.BOOLEAN);

        assertDoesNotThrow(() -> Boolean.parseBoolean(json));
    }

    @Test
    public void generate_Integer_GeneratesInteger() {
        String json = producer().generate(JsonValueType.INTEGER);

        assertDoesNotThrow(() -> Integer.parseInt(json));
    }

    @Test
    public void generate_Number_GeneratesNumber() {
        String json = producer().generate(JsonValueType.NUMBER);

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
