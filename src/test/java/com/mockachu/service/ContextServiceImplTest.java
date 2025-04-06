package com.mockachu.service;

import com.mockachu.template.MockVariables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ContextServiceImplTest {

    private ContextService getService() {
        return new ContextServiceImpl();
    }

    @Test
    void WHEN_getVariables_THEN_emptyVariables() {
        var service = getService();
        assertEquals(0, service.get().size());
    }

    @Test
    void WHEN_putTwoVariablesAndGetVariables_THEN_twoVariableWithCorrectValues() {
        var service = getService();
        service.put("x", "10");
        service.put("id", "5");
        assertEquals(2, service.get().size());
        assertEquals("10", service.get().get("x"));
        assertEquals("5", service.get().get("id"));
        assertTrue(service.getAsString().contains("x = 10"));
        assertTrue(service.getAsString().contains("id = 5"));
    }

    @Test
    void WHEN_setTwoVariablesFromStringAndGetVariables_THEN_twoVariableWithCorrectValues() {
        var service = getService();
        service.setFromString("""
                x=10
                            
                id   =        5
                not a variable assignment
                """);
        assertEquals(2, service.get().size());
        assertEquals("10", service.get().get("x"));
        assertEquals("5", service.get().get("id"));
    }

    @Test
    void WHEN_setVariablesFromEmptyStringAndGetVariables_THEN_noVariables() {
        var service = getService();

        service.put("x", "test");
        service.setFromString("");
        assertEquals(0, service.get().size());

        service.put("x", "test");
        service.setFromString(null);
        assertEquals(0, service.get().size());
    }

    @Test
    void WHEN_putTwoVariablesFromMockVariablesAndGetVariables_THEN_twoVariableWithCorrectValues() {
        var service = getService();
        service.putAll(new MockVariables().put("x", "10").put("id", "5"));
        assertEquals(2, service.get().size());
        assertEquals("10", service.get().get("x"));
        assertEquals("5", service.get().get("id"));
    }
}
