package com.mockservice.domain;

import com.mockservice.exception.ScenarioParseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.junit.jupiter.api.Assertions.*;

public class ScenarioTest {

    private static final String STR_1 = "1";
    private static final String STR_2 = "2";

    @Test
    public void setGroup_Null_GroupIsEmptyString() {
        assertTrue(new Scenario().setGroup(null).getGroup().isEmpty());
    }

    @Test
    public void setAlias_Null_AliasIsEmptyString() {
        assertTrue(new Scenario().setAlias(null).getAlias().isEmpty());
    }

    @Test
    public void setType_Null_TypeIsMap() {
        assertEquals(ScenarioType.MAP, new Scenario().setType(null).getType());
    }

    @Test
    public void setData_Null_DataIsEmptyString() {
        assertTrue(new Scenario().setData(null).getData().isEmpty());
    }

    @DisplayName("Test Scenario equality based on alias (other parameters don't matter).")
    @Test
    public void equals_SameMethodPathAlt_OtherFieldsDiffer_True() {
        Scenario scenario1 = new Scenario().setAlias(STR_1)
                .setGroup(STR_1)
                .setData(STR_1)
                .setType(ScenarioType.QUEUE);
        Scenario scenario2 = new Scenario().setAlias(STR_1)
                .setGroup(STR_2)
                .setData(STR_2)
                .setType(ScenarioType.RING);
        assertEquals(scenario1, scenario2);
    }

    @Test
    public void equals_Null_False() {
        Scenario scenario = new Scenario().setAlias(STR_1);
        assertNotEquals(null, scenario);
    }

    @Test
    public void equals_ObjectOfOtherType_False() {
        Scenario scenario = new Scenario().setAlias(STR_1);
        assertNotEquals(scenario, new Object());
    }

    @Test
    public void hashCode_EqualsForEqualObjects() {
        Scenario scenario1 = new Scenario().setGroup(STR_1).setAlias(STR_1);
        Scenario scenario2 = new Scenario().setGroup(STR_1).setAlias(STR_1);
        assertEquals(scenario1.hashCode(), scenario2.hashCode());
    }

    @Test
    public void compareTo_Equal() {
        Scenario scenario1 = new Scenario().setGroup(STR_1).setAlias(STR_1);
        Scenario scenario2 = new Scenario().setGroup(STR_1).setAlias(STR_1);
        assertEquals(0, scenario1.compareTo(scenario2));
    }

    @Test
    public void compareTo_ByGroup() {
        Scenario scenario1 = new Scenario().setGroup(STR_1).setAlias(STR_1);
        Scenario scenario2 = new Scenario().setGroup(STR_2).setAlias(STR_1);
        assertTrue(0 > scenario1.compareTo(scenario2));
    }

    @Test
    public void compareTo_ByType() {
        Scenario scenario1 = new Scenario().setGroup(STR_1).setAlias(STR_1);
        Scenario scenario2 = new Scenario().setGroup(STR_1).setAlias(STR_2);
        assertTrue(0 > scenario1.compareTo(scenario2));
    }

    private static final String METHOD = "GET";
    private static final String PATH = "/test";
    private static final String ALT = "400";
    private static final String SCENARIO_WITH_ALT = METHOD + ";" + PATH + ";" + ALT;
    private static final String SCENARIO_WITH_EMPTY_ALT = METHOD + ";" + PATH;
    private static final String SCENARIO_WITH_EMPTY_LINES = "\n\n" + METHOD + ";" + PATH + ";\"" + ALT + "\n";
    private static final String SCENARIO_WITH_ONE_PARAMETER = METHOD;

    @Test
    public void create_ActivateValidScenario_ParsedSuccessfully() {
        Scenario activeScenario = new Scenario().setData(SCENARIO_WITH_ALT).setActive(true);
        assertTrue(activeScenario.getActive());
        assertTrue(activeScenario.getAltFor(RequestMethod.valueOf(METHOD), PATH).isPresent());
    }

    @Test
    public void create_ActivateValidScenarioWithEmptyAlt_ParsedSuccessfully() {
        Scenario activeScenario = new Scenario().setData(SCENARIO_WITH_EMPTY_ALT).setActive(true);
        assertTrue(activeScenario.getActive());
        assertTrue(activeScenario.getAltFor(RequestMethod.valueOf(METHOD), PATH).isPresent());
    }

    @Test
    public void create_ActivateValidScenarioWithEmptyLine_ParsedSuccessfully() {
        Scenario activeScenario = new Scenario().setData(SCENARIO_WITH_EMPTY_LINES).setActive(true);
        assertTrue(activeScenario.getActive());
        assertTrue(activeScenario.getAltFor(RequestMethod.valueOf(METHOD), PATH).isPresent());
    }

    @Test
    public void create_AssignToActiveScenario_ScenarioActive() {
        Scenario activeScenario = new Scenario().setData(SCENARIO_WITH_ALT).setActive(true);
        Scenario scenario2 = new Scenario().setData(SCENARIO_WITH_EMPTY_ALT);
        activeScenario.assignFrom(scenario2);
        assertTrue(activeScenario.getActive());
        assertTrue(activeScenario.getAltFor(RequestMethod.valueOf(METHOD), PATH).isPresent());
    }

    @Test
    public void create_ActivateInvalidScenarioWithOneParameter_RouteExists() {
        Scenario scenario = new Scenario().setData(SCENARIO_WITH_ONE_PARAMETER);
        assertThrows(ScenarioParseException.class, () -> scenario.setActive(true));
    }
}
