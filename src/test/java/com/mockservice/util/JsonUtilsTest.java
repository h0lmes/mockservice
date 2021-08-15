package com.mockservice.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JsonUtilsTest {

    private static final String UNESCAPED = "\\ \" \b \f \n \r \t";
    private static final String ESCAPED = "\\\\ \\\" \\b \\f \\n \\r \\t";
    private static final String ESCAPED_UNICODE_CHAR = "\\u0041";
    private static final String ESCAPED_ILLEGAL_BACKSLASH = "\\";
    private static final String ESCAPED_ILLEGAL_ESCAPE_CHAR = "\\a";
    private static final String ESCAPED_MALFORMED_UNICODE_CHAR = "\\u555";

    private static final String JSON_SCHEMA = "{\"type\": \"object\",\n" +
            "  \"properties\": {\n" +
            "    \"product_id\": {\"type\": \"integer\"},\n" +
            "    \"name\": {\"type\": \"string\"}\n" +
            "  }}";
    private static final String VALID_JSON = "{\"product_id\": 1, \"name\": \"product name\"}";
    private static final String INVALID_JSON = "{\"product_id\": \"\", \"name\": false}";
    private static final String MALFORMED_JSON_SCHEMA = "{\"type\": \"object\",\n" +
            "  \"properties\": {\n" +
            "    \"product_id\": {\"type\": \"integer\"},\n" +
            "    \"name\": {\"type\": \"string\"}\n" +
            "  }";
    private static final String MALFORMED_JSON = "{\"product_id\": \"\",";

    @Test
    public void escape_ReturnsValidEscapedString() {
        assertEquals(ESCAPED, JsonUtils.escape(UNESCAPED));
    }

    @Test
    public void unescape_ValidEscapedInput_ReturnsValidUnescapedString() {
        assertEquals(UNESCAPED, JsonUtils.unescape(ESCAPED));
    }

    @Test
    public void unescape_ValidUnicodeChar_ReturnsValidUnescapedString() {
        assertEquals("A", JsonUtils.unescape(ESCAPED_UNICODE_CHAR));
    }

    @Test
    public void unescape_IllegalBackslash_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> JsonUtils.unescape(ESCAPED_ILLEGAL_BACKSLASH));
    }

    @Test
    public void unescape_IllegalEscapeChar_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> JsonUtils.unescape(ESCAPED_ILLEGAL_ESCAPE_CHAR));
    }

    @Test
    public void unescape_MalformedUnicodeChar_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> JsonUtils.unescape(ESCAPED_MALFORMED_UNICODE_CHAR));
    }

    @Test
    public void isJson() {
        assertTrue(JsonUtils.isJson(" {} "));
        assertTrue(JsonUtils.isJson(" [] "));
        assertTrue(JsonUtils.isJson("null"));

        assertFalse(JsonUtils.isJson(" "));
        assertFalse(JsonUtils.isJson("<test></test>"));
    }

    @Test
    public void validate_ValidJson_DoesNotThrow() {
        assertDoesNotThrow(() -> JsonUtils.validate(VALID_JSON, JSON_SCHEMA));
    }

    @Test
    public void validate_InvalidJson_ThrowsException() {
        assertThrows(JsonValidationException.class, () -> JsonUtils.validate(INVALID_JSON, JSON_SCHEMA));
    }

    @Test
    public void validate_MalformedJson_ThrowsException() {
        assertThrows(JsonValidationException.class, () -> JsonUtils.validate(MALFORMED_JSON, JSON_SCHEMA));
    }

    @Test
    public void validate_MalformedJsonSchema_ThrowsException() {
        assertThrows(JsonValidationException.class, () -> JsonUtils.validate(VALID_JSON, MALFORMED_JSON_SCHEMA));
    }
}
