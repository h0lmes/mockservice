package com.mockservice.template;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TemplateParserTest {

    private static final String TEXT_WITH_TOKEN_AT_THE_BEGINNING = "${var}---";
    private static final String TEXT_WITH_SEPARATE_TOKEN_DELIMITERS =
            "{- 1 -${var:${random_int:10:20}:${opt:${random_int:20:30}}}$- 2 -${var:${of:TEST:TEXT}}- 3 -}";
    private static final String NESTED_TOKENS = "${var:${random_int:10:20}:${opt:${random_int:20:30}}}";
    private static final String TOKEN_WITH_MISSING_CLOSING_BRACKET = "${var:${opt}";
    private static final String TOKEN_WITH_EXTRA_CLOSING_BRACKET = "${var:${opt}}}";
    private static final String TOKEN_WITH_EMPTY_FIRST_ARGUMENT = "${:default}";
    private static final String TOKEN_EMPTY = "${}";

    @Test
    public void successNoEmptyLineWhenTokenAtTheBeginningTest() {
        List<String> list = TemplateParser.tokenize(TEXT_WITH_TOKEN_AT_THE_BEGINNING);

        assertEquals(2, list.size());
        assertEquals("${var}", list.get(0));
        assertEquals("---", list.get(1));
    }

    @Test
    public void successSeparateTokenDelimitersTokenizeTest() {
        List<String> list = TemplateParser.tokenize(TEXT_WITH_SEPARATE_TOKEN_DELIMITERS);

        assertEquals(5, list.size());
        assertEquals("{- 1 -", list.get(0));
        assertEquals("${var:${random_int:10:20}:${opt:${random_int:20:30}}}", list.get(1));
        assertEquals("$- 2 -", list.get(2));
        assertEquals("${var:${of:TEST:TEXT}}", list.get(3));
        assertEquals("- 3 -}", list.get(4));
    }

    @Test
    public void throwsWhenTokenWithMissingClosingBracketTokenizeTest() {
        assertThrows(IllegalArgumentException.class, () -> TemplateParser.tokenize(TOKEN_WITH_MISSING_CLOSING_BRACKET));
    }

    @Test
    public void throwsWhenEmptyTokenTokenizeTest() {
        assertThrows(IllegalArgumentException.class, () -> TemplateParser.tokenize(TOKEN_EMPTY));
    }

    @Test
    public void successNestedTokensSplitTest() {
        String[] args = TemplateParser.splitToken(NESTED_TOKENS);

        assertEquals(3, args.length);
        assertEquals("var", args[0]);
        assertEquals("${random_int:10:20}", args[1]);
        assertEquals("${opt:${random_int:20:30}}", args[2]);
    }

    @Test
    public void throwsWhenTokenWithMissingClosingBracketSplitTest() {
        assertThrows(IllegalArgumentException.class, () -> TemplateParser.splitToken(TOKEN_WITH_MISSING_CLOSING_BRACKET));
    }

    @Test
    public void throwsWhenTokenWithExtraClosingBracketSplitTest() {
        assertThrows(IllegalArgumentException.class, () -> TemplateParser.splitToken(TOKEN_WITH_EXTRA_CLOSING_BRACKET));
    }

    @Test
    public void throwsWhenTokenWithEmptyFirstArgumentSplitTest() {
        assertThrows(IllegalArgumentException.class, () -> TemplateParser.splitToken(TOKEN_WITH_EMPTY_FIRST_ARGUMENT));
    }
}
