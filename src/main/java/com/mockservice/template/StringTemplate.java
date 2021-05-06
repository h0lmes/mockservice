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

    private static final String NEW_LINE = System.lineSeparator();
    private static final TemplateFunction[] templateFunctions = TemplateFunction.values();

    /**
     * Internal list holds data in the following way:
     * - each line holds a text only (with possible line breaks) or a token only
     * - one token per line
     * - there could be no consecutive text lines, though could be consecutive lines holding tokens
     */
    private final List<String> strings = new ArrayList<>();
    private State state = State.EMPTY;

    public StringTemplate() {}

    public StringTemplate(String content) {
        add(content);
    }

    // parser

    public void add(String line) {
        List<String> tokens = TemplateParser.tokenize(line);
        if (!State.EMPTY.equals(state)) {
            putToken(NEW_LINE);
        }

        for (String token : tokens) {
            putToken(token);
        }
    }

    private void putToken(String token) {
        if (TemplateParser.isToken(token)) {
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

    private static String map(String token, @Nullable Map<String, String> variables, Map<String, Function<String[], String>> functions) {
        if (TemplateParser.isToken(token)) {
            String[] args = TemplateParser.tokenToArguments(token);

            if (variables != null && variables.containsKey(args[0])) {
                String var = variables.get(args[0]);
                while (TemplateParser.isToken(var)) {
                    var = map(var, variables, functions);
                }
                return var;
            }

            if (functions != null && functions.containsKey(args[0])) {
                return functions.get(args[0]).apply(args);
            }

            if (args.length > 1) {
                String var = args[1];
                while (TemplateParser.isToken(var)) {
                    var = map(var, variables, functions);
                }
                return var;
            }
        }
        return token;
    }
}
