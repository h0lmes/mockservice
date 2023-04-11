package com.mockservice.template;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StringTemplateTest {

    private static final String LINE_BREAK = System.lineSeparator();
    private static final String STR_1 = "one";
    private static final String STR_2 = "two";
    private static final String STR_3 = "three";

    private MockVariables variablesEmpty() {
        return new MockVariables();
    }

    private MockVariables variablesOf(String name, String value) {
        MockVariables map = new MockVariables();
        map.put(name, value);
        return map;
    }

    private MockVariables variablesWithInner(String name, String innerVarName, String innerVarValue) {
        MockVariables map = new MockVariables();
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

    private MockFunctions functionsEmpty() {
        return new MockFunctions();
    }

    private MockFunctions function(String name, String value) {
        MockFunctions map = new MockFunctions();
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
        MockVariables variables = variablesWithInner(STR_1, STR_2, STR_3);
        String result = template.toString(variables, functionsEmpty());

        assertEquals(STR_3, result);
    }

    @Test
    public void toString_VariableWhichDefaultValueIsAnotherVariable_VariableReplacedWithValue() {
        StringTemplate template = new StringTemplate();
        template.add(templateVariable(STR_1, templateVariable(STR_2)));
        MockVariables variables = variablesOf(STR_2, STR_3);
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
