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
}
