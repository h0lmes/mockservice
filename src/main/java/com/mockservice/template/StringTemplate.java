package com.mockservice.template;

import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class StringTemplate {

    private enum State {
        EMPTY, TEXT, VARIABLE
    }

    private static final String VAR_START = "${";
    private static final String VAR_END = "}";
    private static final String NEW_LINE = System.lineSeparator();

    private final List<String> strings = new ArrayList<>();
    private State state = State.EMPTY;

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
        return !s.isEmpty() && s.startsWith(VAR_START);
    }

    private List<String> tokenize(String line) {
        List<String> tokens = new ArrayList<>();
        int len = line.length();
        int at = 0;
        while (at < len) {
            int start = line.indexOf(VAR_START, at);
            if (start == at) {
                int end = line.indexOf(VAR_END, start);
                if (end > at + VAR_START.length()) {
                    end += VAR_END.length();
                    tokens.add(line.substring(start, end));
                    at = end;
                } else {
                    throw new IllegalArgumentException("Invalid token at position " + start + " in:" + NEW_LINE + line);
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        strings.forEach(builder::append);
        return builder.toString();
    }

    public String toString(@Nullable Map<String, String> variables, @Nullable Map<String, Supplier<String>> suppliers) {
        StringBuilder builder = new StringBuilder();
        strings.forEach(s -> builder.append(map(s, variables, suppliers)));
        return builder.toString();
    }

    private String map(String token, @Nullable Map<String, String> variables, @Nullable Map<String, Supplier<String>> suppliers) {
        if (isTokenVariable(token)) {
            String defVal = token;
            String name = token.substring(VAR_START.length(), token.length() - VAR_END.length());
            int colon = name.indexOf(':');
            if (colon >= 0) {
                defVal = name.substring(colon + 1);
                name = name.substring(0, colon);
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Token name must not be empty (" + token + ")");
            }
            if (variables != null && variables.containsKey(name)) {
                return variables.get(name);
            }
            if (suppliers != null && suppliers.containsKey(name)) {
                return suppliers.get(name).get();
            }
            return defVal;
        }
        return token;
    }
}
