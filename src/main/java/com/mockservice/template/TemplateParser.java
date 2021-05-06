package com.mockservice.template;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TemplateParser {

    private static final String VAR_START = "${";
    private static final int VAR_START_LEN = VAR_START.length();
    private static final String VAR_END = "}";
    private static final int VAR_END_LEN = VAR_END.length();

    private static final char VAR_START_CHAR = '$';
    private static final char OPENING_BRACKET_CHAR = '{';
    private static final char CLOSING_BRACKET_CHAR = '}';
    private static final char ARGS_SPLIT_CHAR = ':';

    public static boolean isToken(String s) {
        return s.startsWith(VAR_START);
    }

    public static List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        StringBuilder token = new StringBuilder();
        int brackets = 0;

        char[] chars = text.toCharArray();
        int len = chars.length;
        char ch;

        for (int index = 0; index < len; index++) {
            ch = chars[index];
            if (ch == VAR_START_CHAR) {
                if (startOfTokenAt(index, chars, len)) {
                    if (brackets == 0 && token.length() > 0) {
                        tokens.add(token.toString());
                        token.setLength(0);
                    }
                    brackets++;
                }
                token.append(ch);
            } else if (ch == CLOSING_BRACKET_CHAR) {
                token.append(ch);
                brackets--;
                if (brackets <= 0) {
                    tokens.add(token.toString());
                    token.setLength(0);
                    brackets = 0;
                }
            } else {
                token.append(ch);
            }
        }
        if (token.length() > 0) {
            tokens.add(token.toString());
        }

        if (brackets > 0) {
            throw new IllegalArgumentException("Invalid token(s) in: " + text);
        }
        return tokens;
    }

    private static boolean startOfTokenAt(int index, char[] chars, int len) {
        return chars[index] == VAR_START_CHAR && index + 1 < len && chars[index + 1] == OPENING_BRACKET_CHAR;
    }

    public static String[] tokenToArguments(String token) {
        token = token.substring(VAR_START_LEN, token.length() - VAR_END_LEN);

        List<String> argsList = new ArrayList<>();
        StringBuilder argument = new StringBuilder();
        int brackets = 0;
        for (char ch : token.toCharArray()) {
            if (ch == OPENING_BRACKET_CHAR) {
                brackets++;
            }
            if (ch == CLOSING_BRACKET_CHAR) {
                brackets--;
                if (brackets < 0) {
                    throw new IllegalArgumentException("Invalid token " + token);
                }
            }

            if (brackets == 0 && ch == ARGS_SPLIT_CHAR) {
                argsList.add(argument.toString());
                argument.setLength(0);
            } else {
                argument.append(ch);
            }
        }
        argsList.add(argument.toString());
        if (brackets > 0) {
            throw new IllegalArgumentException("Invalid token " + token);
        }

        String[] args = argsList.toArray(new String[]{});
        if (args[0].isEmpty()) {
            throw new IllegalArgumentException(String.format("Token arg[0] must not be empty: %s", token));
        }

        return args;
    }

    public static void main(String[] args) {
        tokenize("${var}---").forEach(System.out::println);
        System.out.println("----------------------------------------");
        tokenize("{- 1 -${var:${random_int:10:20}:${opt:${random_int:20:30}}}$- 2 -${var:${of:TEST:TEXT}}- 3 -}").forEach(System.out::println);
        System.out.println("----------------------------------------");
        Arrays.stream(tokenToArguments("${var:${random_int:10:20}:${opt:${random_int:20:30}}}")).forEach(System.out::println);
    }
}
