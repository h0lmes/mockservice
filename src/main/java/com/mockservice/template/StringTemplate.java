package com.mockservice.template;

import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * The {@code StringTemplate} class represents parsed template.
 * <p>
 * Supports only addition of strings.
 * The format of variables:
 * - ${var_name}
 * - ${var_name:def_val}
 * - ${function_name}
 * - ${function_name:param1:param2:....}
 *
 * @author  Roman Dubinin
 */
public class StringTemplate {

    private enum State {
        EMPTY, // internal list is empty
        TEXT, // last line is a text
        VARIABLE // last line is a variable
    }

    private static final String VAR_START = "${";
    private static final int VAR_START_LEN = VAR_START.length();
    private static final String VAR_END = "}";
    private static final int VAR_END_LEN = VAR_END.length();
    private static final String VAR_SPLIT = ":";
    private static final String NEW_LINE = System.lineSeparator();

    private static final TemplateFunction[] templateFunctions = TemplateFunction.values();

    /**
     * Internal list holds data in the following way:
     * - each line holds a text only (with possible line breaks) or a variable only
     * - one variable per line
     * - there could be no consecutive text lines, though could be consecutive lines holding variables (for performance).
     */
    private final List<String> strings = new ArrayList<>();
    private State state = State.EMPTY;

    public StringTemplate() {}

    public StringTemplate(String content) {
        add(content);
    }

    // parser

    public void add(String line) {
        List<String> tokens = tokenize(line);
        if (!isEmpty()) {
            putToken(NEW_LINE);
        }
        putTokens(tokens);
    }

    private boolean isEmpty() {
        return State.EMPTY.equals(state);
    }

    private boolean isText() {
        return State.TEXT.equals(state);
    }

    private void putTokens(List<String> tokens) {
        for (String token : tokens) {
            putToken(token);
        }
    }

    private void putToken(String token) {
        if (isTokenVariable(token)) {
            strings.add(token);
            state = State.VARIABLE;
        } else {
            if (isText()) {
                appendToLast(token);
            } else {
                strings.add(token);
                state = State.TEXT;
            }
        }
    }

    private void appendToLast(String str) {
        int last = strings.size() - 1;
        strings.set(last, strings.get(last) + str);
    }

    private boolean isTokenVariable(String s) {
        return s.startsWith(VAR_START);
    }

    private List<String> tokenize(String line) {
        List<String> tokens = new ArrayList<>();
        int len = line.length();
        int at = 0;
        while (at < len) {
            int start = line.indexOf(VAR_START, at);
            if (start == at) {
                int end = line.indexOf(VAR_END, start);
                if (end > at + VAR_START_LEN) {
                    end += VAR_END_LEN;
                    tokens.add(line.substring(start, end));
                    at = end;
                } else {
                    throw new IllegalArgumentException(String.format("Invalid token at position %d in:%n%s", start, line));
                }
            } else {
                if (start < 0) {
                    start = len;
                }
                tokens.add(line.substring(at, start));
                at = start;
            }
        }
        if (tokens.isEmpty()) {
            tokens.add("");
        }
        return tokens;
    }

    // builder

    public String toString(@Nullable Map<String, String> variables) {
        // per request, to support stateful functions (e.g. sequence)
        Map<String, Function<String[], String>> functions = makeFunctions();

        StringBuilder builder = new StringBuilder();
        strings.forEach(s -> builder.append(map(s, variables, functions)));
        return builder.toString();
    }

    private Map<String, Function<String[], String>> makeFunctions() {
        Map<String, Function<String[], String>> functions = new HashMap<>();
        for (TemplateFunction function : templateFunctions) {
            functions.put(function.getName(), function.getFunction());
        }
        return functions;
    }

    private String map(String token, @Nullable Map<String, String> variables, Map<String, Function<String[], String>> functions) {
        if (isTokenVariable(token)) {
            String[] args = token
                    .substring(VAR_START_LEN, token.length() - VAR_END_LEN)
                    .split(VAR_SPLIT);
            if (args[0].isEmpty()) {
                throw new IllegalArgumentException(String.format("Token must not be empty: %s", token));
            }

            if (variables != null && variables.containsKey(args[0])) {
                return variables.get(args[0]);
            }
            if (functions != null && functions.containsKey(args[0])) {
                return functions.get(args[0]).apply(args);
            }

            if (args.length > 1) {
                return args[1];
            }
        }
        return token;
    }
}
