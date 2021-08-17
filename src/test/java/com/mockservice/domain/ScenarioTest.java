package com.mockservice.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
                .setType(ScenarioType.CIRCULAR_QUEUE);
        assertEquals(scenario1, scenario2);
    }
}
