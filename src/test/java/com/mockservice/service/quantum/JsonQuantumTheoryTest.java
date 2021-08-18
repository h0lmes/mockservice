package com.mockservice.service.quantum;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockservice.producer.JsonProducerImpl;
import com.mockservice.producer.ValueProducerImpl;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class JsonQuantumTheoryTest {

    private static final String VALID_JSON = "{\"product_id\": 1, \"label\": \"label\", \"in_stock\": true}";
    private static final String EMPTY_OBJECT_JSON = "{}";
    private static final String NOT_A_JSON = "<test>test</test>";

    private QuantumTheory theory() {
        ValueProducerImpl valueProducer = new ValueProducerImpl();
        return new JsonQuantumTheory(valueProducer, new JsonProducerImpl(valueProducer));
    }

    @Test
    public void applicable_ValidJson_ReturnsTrue() {
        assertTrue(theory().applicable(VALID_JSON));
    }

    @Test
    public void applicable_NotAJson_ReturnsFalse() {
        assertFalse(theory().applicable(NOT_A_JSON));
    }

    @RepeatedTest(10)
    public void apply_ToValidJson_ReturnsDeserializableJson() {
        String json = theory().apply(VALID_JSON);
        ObjectMapper mapper = new ObjectMapper();
        assertDoesNotThrow(() -> mapper.readTree(json));
    }

    @Test
    public void apply_ToEmptyObjectJson_ReturnsDeserializableJson() {
        String json = theory().apply(EMPTY_OBJECT_JSON);
        ObjectMapper mapper = new ObjectMapper();
        assertDoesNotThrow(() -> mapper.readTree(json));
    }
}
