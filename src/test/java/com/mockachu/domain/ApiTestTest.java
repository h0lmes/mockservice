package com.mockachu.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiTestTest {

    private static final RequestMethod METHOD1 = RequestMethod.GET;
    private static final RequestMethod METHOD2 = RequestMethod.PUT;
    private static final String STR_1 = "1";
    private static final String STR_2 = "2";

    @Test
    void setGroup_Null_GroupIsEmptyString() {
        assertTrue(new ApiTest().setGroup(null).getGroup().isEmpty());
    }

    @Test
    void setAlias_Null_AliasIsEmptyString() {
        assertTrue(new ApiTest().setAlias(null).getAlias().isEmpty());
    }

    @Test
    void setPlan_Null_PlanIsEmptyString() {
        assertTrue(new ApiTest().setPlan(null).getPlan().isEmpty());
    }

    @DisplayName("Test Route equality based on method, path and alt (other parameters don't matter).")
    @Test
    void equals_SameId_OtherFieldsDiffer_True() {
        var v1 = new ApiTest().setPlan(STR_1).setAlias(STR_1).setGroup(STR_1);
        var v2 = new ApiTest().setPlan(STR_1).setAlias(STR_1).setGroup(STR_1);
        assertEquals(v1, v2);
    }

    @Test
    void hashCode_EqualsForEqualObjects() {
        var v1 = new ApiTest().setPlan(STR_1).setAlias(STR_1).setGroup(STR_1);
        var v2 = new ApiTest().setPlan(STR_1).setAlias(STR_1).setGroup(STR_1);
        assertEquals(v1.hashCode(), v2.hashCode());
    }

    @Test
    void compareTo_Equal() {
        var v1 = new ApiTest().setPlan(STR_1).setAlias(STR_1).setGroup(STR_1);
        var v2 = new ApiTest().setPlan(STR_1).setAlias(STR_1).setGroup(STR_1);
        assertEquals(0, v1.compareTo(v2));
    }

    @Test
    void compareTo_ByGroup() {
        var v1 = new ApiTest().setPlan(STR_1).setAlias(STR_1).setGroup(STR_1);
        var v2 = new ApiTest().setPlan(STR_1).setAlias(STR_1).setGroup(STR_2);
        assertTrue(0 > v1.compareTo(v2));
    }

    @Test
    void compareTo_ByAlias() {
        var v1 = new ApiTest().setPlan(STR_1).setAlias(STR_1).setGroup(STR_1);
        var v2 = new ApiTest().setPlan(STR_1).setAlias(STR_2).setGroup(STR_1);
        assertTrue(0 > v1.compareTo(v2));
    }

    @Test
    void compareAfterAssignFrom() {
        var v1 = new ApiTest().setPlan(STR_1).setAlias(STR_1).setGroup(STR_1);
        var v2 = new ApiTest().assignFrom(v1);
        assertEquals(v1.getGroup(), v2.getGroup());
        assertEquals(v1.getAlias(), v2.getAlias());
        assertEquals(v1.getPlan(), v2.getPlan());
    }

    @Test
    void correctToString() {
        var v = new ApiTest().setGroup(STR_1).setAlias(STR_2);
        assertTrue(v.toString().contains(STR_1));
        assertTrue(v.toString().contains(STR_2));
    }
}
