package com.mockservice.template;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MockVariablesTest {

    private static final String STR1 = "AAA";
    private static final String STR2 = "BBB";

    @Test
    void put_OneItem_ContainsKey() {
        MockVariables variables = new MockVariables();
        variables.put(STR1, STR1);
        assertTrue(variables.containsKey(STR1));
    }

    @Test
    void put_OneItem_GetReturnsValue() {
        MockVariables variables = new MockVariables();
        variables.put(STR1, STR1);
        assertEquals(STR1, variables.get(STR1));
    }

    @Test
    void put_OneItem_NotEmpty() {
        MockVariables variables = new MockVariables();
        variables.put(STR1, STR1);
        assertFalse(variables.isEmpty());
    }

    @Test
    void put_OneItem_SizeEqualsOne() {
        MockVariables variables = new MockVariables();
        variables.put(STR1, STR1);
        assertEquals(1, variables.size());
    }

    @Test
    void put_OneItemThenRemove_Empty() {
        MockVariables variables = new MockVariables();
        variables.put(STR1, STR1);
        variables.remove(STR1);
        assertTrue(variables.isEmpty());
    }

    @Test
    void put_OneItemThenClear_Empty() {
        MockVariables variables = new MockVariables();
        variables.put(STR1, STR1);
        variables.clear();
        assertTrue(variables.isEmpty());
    }

    @Test
    void putAll_NullVariables_Empty() {
        MockVariables variables = new MockVariables();
        variables.putAll((MockVariables) null);
        assertTrue(variables.isEmpty());
    }

    @Test
    void putAll_Variables_GetReturnsValue() {
        MockVariables variables1 = new MockVariables();
        variables1.put(STR1, STR1);

        MockVariables variables = new MockVariables();
        variables.putAll(variables1);
        assertEquals(STR1, variables.get(STR1));
    }

    @Test
    void putAll_NullMap_Empty() {
        MockVariables variables = new MockVariables();
        variables.putAll((Map<String, String>) null);
        assertTrue(variables.isEmpty());
    }

    @Test
    void putAll_Map_GetReturnsValue() {
        Map<String, String> map = new HashMap<>();
        map.put(STR1, STR1);

        MockVariables variables = new MockVariables();
        variables.putAll(map);
        assertEquals(STR1, variables.get(STR1));
    }

    @Test
    void sum_ofTwo_ReturnsAllVariables() {
        var v1 = new MockVariables().put(STR1, STR1);
        var v2 = new MockVariables().put(STR2, STR2);
        var sum = MockVariables.sum(v1, v2);
        assertEquals(2, sum.size());
        assertEquals(STR1, sum.get(STR1));
        assertEquals(STR2, sum.get(STR2));
    }

    @Test
    void sum_ofFirstOnly_ReturnsFirst() {
        var v1 = new MockVariables().put(STR1, STR1);
        var sum = MockVariables.sum(v1, null);
        assertEquals(1, sum.size());
        assertEquals(STR1, sum.get(STR1));
    }

    @Test
    void sum_ofSecondOnly_ReturnsSecond() {
        var v2 = new MockVariables().put(STR2, STR2);
        var sum = MockVariables.sum(null, v2);
        assertEquals(1, sum.size());
        assertEquals(STR2, sum.get(STR2));
    }

    @Test
    void sum_ofNone_ReturnsNull() {
        assertNull(MockVariables.sum(null, null));
    }

    @Test
    void get_existing_returnsValue() {
        var v1 = new MockVariables().put(STR1, STR1);
        assertEquals(STR1, MockVariables.get(v1, STR1));
    }

    @Test
    void get_nonExisting_returnsNullString() {
        var v1 = new MockVariables().put(STR1, STR1);
        assertEquals("null", MockVariables.get(v1, STR2));
    }

    @Test
    void get_fromNullMockVariable_returnsNullString() {
        assertEquals("null", MockVariables.get(null, STR1));
    }
}
