package com.mockservice.template;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code StringTemplate} class represents parsed template.
 * <p>
 * Token format:
 * - ${var_name}
 * - ${var_name:def_val}
 * - ${function_name}
 * - ${function_name:param1:param2:....}
 *
 * @author  Roman Dubinin
 */
public class StringTemplate {

    private enum State {
        EMPTY, // this template is empty
        TEXT, // last line is a text
        TOKEN // last line is a token
    }

    /**
     * Internal list of strings holds data in the following way:
     * - a line can contain text (line breaks allowed)
     * - a line can contain a token (exactly one)
     * - there could be no consecutive text lines
     * - there could be consecutive lines with tokens
     */
    private final List<String> strings = new ArrayList<>();
    private State state = State.EMPTY;

    // parser

    public void add(String line) {
        List<String> tokens = TokenParser.tokenize(line);
        if (!State.EMPTY.equals(state)) {
            putToken(System.lineSeparator());
        }

        for (String token : tokens) {
            putToken(token);
        }
    }

    private void putToken(String token) {
        if (TokenParser.isToken(token)) {
            strings.add(token);
            state = State.TOKEN;
        } else {
            if (State.TEXT.equals(state)) {
                int last = strings.size() - 1;
                strings.set(last, strings.get(last) + token);
            } else {
                strings.add(token);
                state = State.TEXT;
            }
        }
    }

    // builder

    public String toString(MockVariables variables, MockFunctions functions) {
        StringBuilder builder = new StringBuilder();
        for (String s : strings) {
            String value = map(s, variables, functions);
            if (value == null) {
                value = "null";
            }
            builder.append(value);
        }
        return builder.toString();
    }

    private static String map(String token, MockVariables variables, MockFunctions functions) {
        if (TokenParser.isToken(token)) {
            String[] args = TokenParser.parseToken(token);

            if (variables.containsKey(args[0])) {
                String var = variables.get(args[0]);
                while (TokenParser.isToken(var)) {
                    var = map(var, variables, functions);
                }
                return var;
            }

            if (functions.containsKey(args[0])) {
                return functions.get(args[0]).apply(args);
            }

            if (args.length > 1) {
                String var = args[1];
                while (TokenParser.isToken(var)) {
                    var = map(var, variables, functions);
                }
                return var;
            }
        }
        return token;
    }
}
