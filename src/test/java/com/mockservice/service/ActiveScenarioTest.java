package com.mockservice.service;

import com.mockservice.domain.Scenario;
import com.mockservice.domain.ScenarioParseException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ActiveScenarioTest {

    private static final String SCENARIO_WITH_ALT = "GET;/test;400";
    private static final String SCENARIO_WITH_EMPTY_ALT = "GET;/test";
    private static final String SCENARIO_WITH_EMPTY_LINES = "\n\nGET;/test;400\n";
    private static final String SCENARIO_WITH_ONE_PARAMETER = "GET";

    @Test
    public void create_ValidScenario_RouteExists() {
        ActiveScenario activeScenario = new ActiveScenario(new Scenario().setData(SCENARIO_WITH_ALT));

        assertEquals(1, activeScenario.getRoutes().size());
    }

    @Test
    public void create_ValidScenarioWithEmptyAlt_RouteExists() {
        ActiveScenario activeScenario = new ActiveScenario(new Scenario().setData(SCENARIO_WITH_EMPTY_ALT));

        assertEquals(1, activeScenario.getRoutes().size());
    }

    @Test
    public void create_ValidScenarioWithEmptyLine_RouteExists() {
        ActiveScenario activeScenario = new ActiveScenario(new Scenario().setData(SCENARIO_WITH_EMPTY_LINES));

        assertEquals(1, activeScenario.getRoutes().size());
    }

    @Test
    public void create_InvalidScenarioWithOneParameter_RouteExists() {
        assertThrows(ScenarioParseException.class,
                () -> new ActiveScenario(new Scenario().setData(SCENARIO_WITH_ONE_PARAMETER)));
    }
}
