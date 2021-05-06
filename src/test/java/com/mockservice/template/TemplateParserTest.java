package com.mockservice.template;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TemplateParserTest {

    private static final String TEXT_WITH_TOKEN = "${var}---";
    private static final String TEXT_WITH_SEPARATE_TOKEN_DELIMITERS =
            "{- 1 -${var:${random_int:10:20}:${opt:${random_int:20:30}}}$- 2 -${var:${of:TEST:TEXT}}- 3 -}";
    private static final String NESTED_TOKENS = "${var:${random_int:10:20}:${opt:${random_int:20:30}}}";

    @Test
    public void tokenizeWithNoEmptyLineAtTheBeginningTest() {
        List<String> list = TemplateParser.tokenize(TEXT_WITH_TOKEN);

        assertEquals(2, list.size());
        assertEquals("${var}", list.get(0));
        assertEquals("---", list.get(1));
    }

    @Test
    public void tokenizeWithNoFaultOnSeparateTokenDelimitersTest() {
        List<String> list = TemplateParser.tokenize(TEXT_WITH_SEPARATE_TOKEN_DELIMITERS);

        assertEquals(5, list.size());
        assertEquals("{- 1 -", list.get(0));
        assertEquals("${var:${random_int:10:20}:${opt:${random_int:20:30}}}", list.get(1));
        assertEquals("$- 2 -", list.get(2));
        assertEquals("${var:${of:TEST:TEXT}}", list.get(3));
        assertEquals("- 3 -}", list.get(4));
    }

    @Test
    public void parseNestedTokensTest() {
        String[] args = TemplateParser.tokenToArguments(NESTED_TOKENS);

        assertEquals(3, args.length);
        assertEquals("var", args[0]);
        assertEquals("${random_int:10:20}", args[1]);
        assertEquals("${opt:${random_int:20:30}}", args[2]);
    }
}
