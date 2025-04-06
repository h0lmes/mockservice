package com.mockachu.validate;

import com.github.fge.jsonschema.main.JsonSchemaFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonDataValidatorTest {

    private static final String JSON_SCHEMA = """
            {
                "type": "object",
                "properties": {
                    "product_id": {"type": "integer"},
                    "name": {"type": "string"}
                }
            }""";
    private static final String VALID_JSON = "{\"product_id\": 1, \"name\": \"product name\"}";
    private static final String INVALID_JSON = "{\"product_id\": \"\", \"name\": false}";
    private static final String MALFORMED_JSON_SCHEMA = """
            {
                "type": "object",
                "properties": {
                    "product_id": {"type": "integer"},
                    "name": {"type": "string"}
            }""";
    private static final String MALFORMED_JSON = "{\"product_id\": \"\",";

    private DataValidator validator() {
        return new JsonDataValidator(JsonSchemaFactory.byDefault());
    }

    @Test
    void applicable_EmptyObject_ReturnsTrue() {
        assertTrue(validator().applicable("{}"));
    }

    @Test
    void applicable_EmptyArray_ReturnsTrue() {
        assertTrue(validator().applicable("[]"));
    }

    @Test
    void applicable_ValidJson_ReturnsTrue() {
        assertTrue(validator().applicable(VALID_JSON));
    }

    @Test
    void applicable_MalformedJson_ReturnsFalse() {
        assertFalse(validator().applicable(MALFORMED_JSON));
    }

    @Test
    void validate_ValidJson_DoesNotThrow() {
        assertDoesNotThrow(() -> validator().validate(VALID_JSON, JSON_SCHEMA));
    }

    @Test
    void validate_InvalidJson_ThrowsException() {
        assertThrows(DataValidationException.class, () -> validator().validate(INVALID_JSON, JSON_SCHEMA));
    }

    @Test
    void validate_MalformedJson_ThrowsException() {
        assertThrows(DataValidationException.class, () -> validator().validate(MALFORMED_JSON, JSON_SCHEMA));
    }

    @Test
    void validate_MalformedJsonSchema_ThrowsException() {
        assertThrows(DataValidationException.class, () -> validator().validate(VALID_JSON, MALFORMED_JSON_SCHEMA));
    }
}
