package com.mockservice.template;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequestHeadersTemplateTest {

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

    private MockFunctions functionsEmpty() {
        return new MockFunctions();
    }

    private MockFunctions function(String name, String value) {
        MockFunctions map = new MockFunctions();
        map.put(name, (args) -> value);
        return map;
    }

    @Test
    void WHEN_twoHeaders_THEN_mapContainsTwoKeysWithCorrectValues() {
        var template = new RequestHeadersTemplate("""
                Header1: test value
                Header2: ${id}""");
        var map = template.toMap(variablesEmpty(), functionsEmpty());

        assertFalse(map.isEmpty());
        assertEquals(1, map.get("Header1").size());
        assertEquals("test value", map.get("Header1").get(0));
        assertEquals(1, map.get("Header2").size());
        assertEquals("${id}", map.get("Header2").get(0));
    }

    @Test
    void WHEN_headerWithTwoLevelVariable_THEN_variableReplacedWithValue() {
        var template = new RequestHeadersTemplate("""
                Header1: test value
                Header2: ${id}""");
        var map = template.toMap(
                variablesWithInner("id", "inner", "test"),
                functionsEmpty());

        assertFalse(map.isEmpty());
        assertEquals(1, map.get("Header2").size());
        assertEquals("test", map.get("Header2").get(0));
    }

    @Test
    void WHEN_headerWithVariable_THEN_variableReplacedWithFunctionValue() {
        var template = new RequestHeadersTemplate("""
                Header1: test value
                Header2: ${id}""");
        var map = template.toMap(variablesEmpty(), function("id", "test"));

        assertFalse(map.isEmpty());
        assertEquals(1, map.get("Header2").size());
        assertEquals("test", map.get("Header2").get(0));
    }

    @Test
    void WHEN_headerWithVariableToMapThenToString_THEN_returnsTextWithSubstitution() {
        var template = new RequestHeadersTemplate("""
                Header1: test value
                Header2: ${id}""");
        template.toMap(variablesOf("id", "test"), functionsEmpty());
        var text = template.toString();

        assertEquals("""
                Header1: test value
                Header2: test""", text);
    }

    @Test
    void WHEN_headerWithNoVariablesAndEmptyLine_THEN_returnsTextAsIsIgnoringEmptyLine() {
        var template = new RequestHeadersTemplate("""
                Header1: value 1
                
                Header2: value 2""");
        var text = template.toString();

        assertEquals("""
                Header1: value 1
                Header2: value 2""", text);
    }

    @Test
    void WHEN_headersEmpty_THEN_empty() {
        var template = new RequestHeadersTemplate("  ");
        assertTrue(template.isEmpty());
        assertTrue(template.toMap(null, null).isEmpty());
        assertTrue(template.toString().isEmpty());
    }
}
