package com.mockachu.template;

import java.util.ArrayList;
import java.util.List;

public class TokenParser {

    private static final String TOKEN_START = "${";
    private static final int TOKEN_START_LEN = TOKEN_START.length();
    private static final String TOKEN_END = "}";
    private static final int TOKEN_END_LEN = TOKEN_END.length();

    private static final char TOKEN_START_CHAR = '$';
    private static final char OPENING_BRACKET_CHAR = '{';
    private static final char CLOSING_BRACKET_CHAR = '}';
    private static final char TOKEN_ARGS_SPLIT_CHAR = ':';

    private TokenParser() {
        // private
    }

    public static boolean isToken(String str) {
        return str != null && str.startsWith(TOKEN_START) && str.endsWith(TOKEN_END);
    }

    public static List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        StringBuilder token = new StringBuilder();

        char[] chars = text.toCharArray();
        int len = chars.length;
        char ch;
        int level = 0;

        for (int index = 0; index < len; index++) {
            ch = chars[index];

            if (startOfTokenAt(index, chars, len)) {
                collectToken(level == 0, token, tokens);
                level++;
                token.append(ch);
            } else if (ch == CLOSING_BRACKET_CHAR && level == 1) {
                token.append(ch);
                level = 0;
                assertTokenLength(token, index, text);
                collectToken(true, token, tokens);
            } else if (ch == CLOSING_BRACKET_CHAR && level > 1) {
                token.append(ch);
                level--;
            } else {
                token.append(ch);
            }
        }
        collectToken(true, token, tokens);

        if (level > 0) {
            throw new IllegalArgumentException("End of token expected in: " + text);
        }
        return tokens;
    }

    private static boolean startOfTokenAt(int index, char[] chars, int len) {
        return chars[index] == TOKEN_START_CHAR && index + 1 < len && chars[index + 1] == OPENING_BRACKET_CHAR;
    }

    private static void assertTokenLength(StringBuilder token, int index, String text) {
        if (token.length() <= TOKEN_START_LEN + TOKEN_END_LEN) {
            throw new IllegalArgumentException("Unexpected end of token at " + index + " in: " + text);
        }
    }

    private static void collectToken(boolean allowCollect, StringBuilder token, List<String> tokens) {
        if (allowCollect && token.length() > 0) {
            tokens.add(token.toString());
            token.setLength(0);
        }
    }

    /**
     * @param token assumes a valid token as input.
     * @return an array of token arguments (arguments are character sequences separated by TOKEN_ARGS_SPLIT_CHAR)
     * with respect that an argument may be a valid token itself (token nesting)
     */
    public static String[] parseToken(String token) {
        token = token.substring(TOKEN_START_LEN, token.length() - TOKEN_END_LEN);

        List<String> arguments = new ArrayList<>();
        StringBuilder argument = new StringBuilder();
        int brackets = 0;

        for (char ch : token.toCharArray()) {
            if (ch == OPENING_BRACKET_CHAR) {
                brackets++;
            }
            if (ch == CLOSING_BRACKET_CHAR) {
                brackets--;
                if (brackets < 0) {
                    throw new IllegalArgumentException("Invalid token (unexpected token closing char): " + token);
                }
            }

            if (brackets == 0 && ch == TOKEN_ARGS_SPLIT_CHAR) {
                arguments.add(argument.toString());
                argument.setLength(0);
            } else {
                argument.append(ch);
            }
        }

        if (brackets > 0) {
            throw new IllegalArgumentException("Invalid token (not closed): " + token);
        }

        arguments.add(argument.toString());

        String[] args = arguments.toArray(new String[]{});
        if (args[0].isEmpty()) {
            throw new IllegalArgumentException("Token arg[0] must not be empty: " + token);
        }

        return args;
    }
}
