package com.mockservice.template;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class StringTemplateTest {

    private static final String LINE_BREAK = System.lineSeparator();
    private static final String STR_1 = "one";
    private static final String STR_2 = "two";
    private static final String STR_3 = "three";

    private Map<String, String> variablesEmpty() {
        return new HashMap<>();
    }

    private Map<String, String> variablesOf(String name, String value) {
        Map<String, String> map = new HashMap<>();
        map.put(name, value);
        return map;
    }

    private Map<String, String> variablesWithInner(String name, String innerVarName, String innerVarValue) {
        Map<String, String> map = new HashMap<>();
        map.put(name, templateVariable(innerVarName));
        map.put(innerVarName, innerVarValue);
        return map;
    }

    private String templateVariable(String name) {
        return "${" + name + "}";
    }

    private String templateVariable(String name, String def) {
        return "${" + name + ":" + def + "}";
    }

    private Map<String, Function<String[], String>> functionsEmpty() {
        return new HashMap<>();
    }

    private Map<String, Function<String[], String>> function(String name, String value) {
        Map<String, Function<String[], String>> map = new HashMap<>();
        map.put(name, (args) -> value);
        return map;
    }

    @Test
    public void add_TwoLinesOfText_LinesSeparatedByALineBreak() {
        StringTemplate template = new StringTemplate();
        template.add(STR_1);
        template.add(STR_2);
        String result = template.toString(variablesEmpty(), functionsEmpty());

        assertTrue(result.contains(LINE_BREAK));
    }

    @Test
    public void toString_Variable_VariableReplacedWithValue() {
        StringTemplate template = new StringTemplate();
        template.add(templateVariable(STR_1));
        String result = template.toString(variablesOf(STR_1, STR_2), functionsEmpty());

        assertEquals(STR_2, result);
    }

    @Test
    public void toString_VariableWhichValueIsAnotherVariable_VariableReplacedWithValue() {
        StringTemplate template = new StringTemplate();
        template.add(templateVariable(STR_1));
        Map<String, String> variables = variablesWithInner(STR_1, STR_2, STR_3);
        String result = template.toString(variables, functionsEmpty());

        assertEquals(STR_3, result);
    }

    @Test
    public void toString_VariableWhichDefaultValueIsAnotherVariable_VariableReplacedWithValue() {
        StringTemplate template = new StringTemplate();
        template.add(templateVariable(STR_1, templateVariable(STR_2)));
        Map<String, String> variables = variablesOf(STR_2, STR_3);
        String result = template.toString(variables, functionsEmpty());

        assertEquals(STR_3, result);
    }

    @Test
    public void toString_VariableValueIsNull_VariableReplacedWithNullString() {
        StringTemplate template = new StringTemplate();
        template.add(templateVariable(STR_1));
        String result = template.toString(variablesOf(STR_1, null), functionsEmpty());

        assertEquals("null", result);
    }

    @Test
    public void toString_Function_FunctionReplacedWithValue() {
        StringTemplate template = new StringTemplate();
        template.add(templateVariable(STR_1));
        String result = template.toString(variablesEmpty(), function(STR_1, STR_2));

        assertEquals(STR_2, result);
    }

    @Test
    public void toString_NoVariablesOrFunctions_ReturnsVariableAsIs() {
        StringTemplate template = new StringTemplate();
        template.add(templateVariable(STR_1));
        String result = template.toString(variablesEmpty(), functionsEmpty());

        assertEquals(templateVariable(STR_1), result);
    }
}
