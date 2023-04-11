package com.mockservice.template;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class MockVariablesTest {

    private static final String STR = "test";

    @Test
    public void put_OneItem_ContainsKey() {
        MockVariables variables = new MockVariables();
        variables.put(STR, STR);
        assertTrue(variables.containsKey(STR));
    }

    @Test
    public void put_OneItem_GetReturnsValue() {
        MockVariables variables = new MockVariables();
        variables.put(STR, STR);
        assertEquals(STR, variables.get(STR));
    }

    @Test
    public void put_OneItem_NotEmpty() {
        MockVariables variables = new MockVariables();
        variables.put(STR, STR);
        assertFalse(variables.isEmpty());
    }

    @Test
    public void put_OneItem_SizeEqualsOne() {
        MockVariables variables = new MockVariables();
        variables.put(STR, STR);
        assertEquals(1, variables.size());
    }

    @Test
    public void put_OneItemThenRemove_Empty() {
        MockVariables variables = new MockVariables();
        variables.put(STR, STR);
        variables.remove(STR);
        assertTrue(variables.isEmpty());
    }

    @Test
    public void put_OneItemThenClear_Empty() {
        MockVariables variables = new MockVariables();
        variables.put(STR, STR);
        variables.clear();
        assertTrue(variables.isEmpty());
    }

    @Test
    public void putAll_NullVariables_Empty() {
        MockVariables variables = new MockVariables();
        variables.putAll((MockVariables) null);
        assertTrue(variables.isEmpty());
    }

    @Test
    public void putAll_Variables_GetReturnsValue() {
        MockVariables variables1 = new MockVariables();
        variables1.put(STR, STR);

        MockVariables variables = new MockVariables();
        variables.putAll(variables1);
        assertEquals(STR, variables.get(STR));
    }

    @Test
    public void putAll_NullMap_Empty() {
        MockVariables variables = new MockVariables();
        variables.putAll((Map<String, String>) null);
        assertTrue(variables.isEmpty());
    }

    @Test
    public void putAll_Map_GetReturnsValue() {
        Map<String, String> map = new HashMap<>();
        map.put(STR, STR);

        MockVariables variables = new MockVariables();
        variables.putAll(map);
        assertEquals(STR, variables.get(STR));
    }
}
