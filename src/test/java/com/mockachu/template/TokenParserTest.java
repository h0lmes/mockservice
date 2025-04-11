package com.mockachu.template;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TokenParserTest {

    private static final String TOKEN_AT_THE_BEGINNING = "{{var}}---";
    private static final String TOKEN_AND_UNRELATED_DELIMITERS =
            "{- 1 -${var:${random_int:10:20}:${opt:${random_int:20:30}}}$- 2 -${var:${of:TEST:TEXT}}- 3 -}";
    private static final String TOKEN_WITH_NESTED_TOKENS = "${var:${random_int:10:20}:${opt:${random_int:20:30}}}";
    private static final String TOKEN_WITH_MISSING_CLOSING_BRACKET = "${var:${opt}";
    private static final String TOKEN_WITH_EXTRA_CLOSING_BRACKET = "${var:${opt}}}";
    private static final String TOKEN_WITH_EMPTY_FIRST_ARGUMENT = "${:default}";
    private static final String TOKEN_EMPTY = "${}";

    // UnitOfWork_StateUnderTest_ExpectedBehavior

    @Test
    void tokenize_TokenAtTheBeginning_FirstLineNotEmpty() {
        List<String> list = TokenParser.tokenize(TOKEN_AT_THE_BEGINNING);

        assertNotEquals("", list.get(0));
    }

    @Test
    void tokenize_TokenAndUnrelatedDelimiters_Success() {
        List<String> list = TokenParser.tokenize(TOKEN_AND_UNRELATED_DELIMITERS);

        assertEquals(5, list.size());
        assertEquals("{- 1 -", list.get(0));
        assertEquals("${var:${random_int:10:20}:${opt:${random_int:20:30}}}", list.get(1));
        assertEquals("$- 2 -", list.get(2));
        assertEquals("${var:${of:TEST:TEXT}}", list.get(3));
        assertEquals("- 3 -}", list.get(4));
    }

    @Test
    void tokenize_TokenWithMissingClosingBracket_Throws() {
        assertThrows(IllegalArgumentException.class, () -> TokenParser.tokenize(TOKEN_WITH_MISSING_CLOSING_BRACKET));
    }

    @Test
    void tokenize_EmptyToken_Throws() {
        assertThrows(IllegalArgumentException.class, () -> TokenParser.tokenize(TOKEN_EMPTY));
    }

    @Test
    void parse_TokenWithNestedTokens_Success() {
        String[] args = TokenParser.parseToken(TOKEN_WITH_NESTED_TOKENS);

        assertEquals(3, args.length);
        assertEquals("var", args[0]);
        assertEquals("${random_int:10:20}", args[1]);
        assertEquals("${opt:${random_int:20:30}}", args[2]);
    }

    @Test
    void parse_TokenWithMissingClosingBracket_Throws() {
        assertThrows(IllegalArgumentException.class, () -> TokenParser.parseToken(TOKEN_WITH_MISSING_CLOSING_BRACKET));
    }

    @Test
    void parse_TokenWithExtraClosingBracket_Throws() {
        assertThrows(IllegalArgumentException.class, () -> TokenParser.parseToken(TOKEN_WITH_EXTRA_CLOSING_BRACKET));
    }

    @Test
    void parse_TokenWithEmptyFirstArgument_Throws() {
        assertThrows(IllegalArgumentException.class, () -> TokenParser.parseToken(TOKEN_WITH_EMPTY_FIRST_ARGUMENT));
    }
}
