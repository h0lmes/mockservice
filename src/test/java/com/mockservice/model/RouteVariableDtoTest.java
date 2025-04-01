package com.mockservice.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.junit.jupiter.api.Assertions.*;

public class RouteVariableDtoTest {

    private static final RequestMethod METHOD1 = RequestMethod.GET;
    private static final RequestMethod METHOD2 = RequestMethod.PUT;

    private static final String STR_1 = "AAA";
    private static final String STR_2 = "BBB";

    @DisplayName("Test Route equality based on method, path and alt (other parameters don't matter).")
    @Test
    public void equals_SameMethodPathAltName_OtherFieldsDiffer_True() {
        RouteVariableDto route1 = new RouteVariableDto().setMethod(METHOD1).setPath(STR_1).setAlt(STR_1)
            .setName(STR_1).setValue(STR_1);
        RouteVariableDto route2 = new RouteVariableDto().setMethod(METHOD1).setPath(STR_1).setAlt(STR_1)
            .setName(STR_1).setValue(STR_1);
        assertEquals(route1, route2);
    }

    @Test
    public void equals_Null_False() {
        RouteVariableDto route = new RouteVariableDto().setMethod(METHOD1).setPath(STR_1).setAlt(STR_1);
        assertNotEquals(null, route);
    }

    @Test
    public void equals_ObjectOfOtherType_False() {
        RouteVariableDto route = new RouteVariableDto().setMethod(METHOD1).setPath(STR_1).setAlt(STR_1);
        assertNotEquals(route, new Object());
    }

    @Test
    public void equals_DifferentMethod_False() {
        RouteVariableDto route1 = new RouteVariableDto().setMethod(METHOD1).setPath(STR_1).setAlt(STR_1);
        RouteVariableDto route2 = new RouteVariableDto().setMethod(METHOD2).setPath(STR_1).setAlt(STR_1);
        assertNotEquals(route1, route2);
    }

    @Test
    public void equals_DifferentPath_False() {
        RouteVariableDto route1 = new RouteVariableDto().setMethod(METHOD1).setPath(STR_1).setAlt(STR_1);
        RouteVariableDto route2 = new RouteVariableDto().setMethod(METHOD1).setPath(STR_2).setAlt(STR_1);
        assertNotEquals(route1, route2);
    }

    @Test
    public void equals_DifferentAlt_False() {
        RouteVariableDto route1 = new RouteVariableDto().setMethod(METHOD1).setPath(STR_1).setAlt(STR_1);
        RouteVariableDto route2 = new RouteVariableDto().setMethod(METHOD1).setPath(STR_1).setAlt(STR_2);
        assertNotEquals(route1, route2);
    }

    @Test
    public void equals_DifferentName_False() {
        RouteVariableDto route1 = new RouteVariableDto().setMethod(METHOD1).setPath(STR_1).setAlt(STR_1).setName(STR_1);
        RouteVariableDto route2 = new RouteVariableDto().setMethod(METHOD1).setPath(STR_1).setAlt(STR_1).setName(STR_2);
        assertNotEquals(route1, route2);
    }

    @Test
    public void hashCode_EqualsForEqualObjects() {
        RouteVariableDto route1 = new RouteVariableDto().setMethod(METHOD1).setPath(STR_1).setAlt(STR_1).setName(STR_1);
        RouteVariableDto route2 = new RouteVariableDto().setMethod(METHOD1).setPath(STR_1).setAlt(STR_1).setName(STR_1);
        assertEquals(route1.hashCode(), route2.hashCode());
    }

    @Test
    public void compareTo_Equal() {
        RouteVariableDto route1 = new RouteVariableDto().setMethod(METHOD1).setPath(STR_1).setAlt(STR_1);
        RouteVariableDto route2 = new RouteVariableDto().setMethod(METHOD1).setPath(STR_1).setAlt(STR_1);
        assertEquals(0, route1.compareTo(route2));
    }

    @Test
    public void compareTo_ByMethod() {
        RouteVariableDto route1 = new RouteVariableDto().setMethod(METHOD1).setPath(STR_1).setAlt(STR_1);
        RouteVariableDto route2 = new RouteVariableDto().setMethod(METHOD2).setPath(STR_1).setAlt(STR_1);
        assertTrue(0 > route1.compareTo(route2));
    }

    @Test
    public void compareTo_ByPath() {
        RouteVariableDto route1 = new RouteVariableDto().setMethod(METHOD1).setPath(STR_1).setAlt(STR_1);
        RouteVariableDto route2 = new RouteVariableDto().setMethod(METHOD1).setPath(STR_2).setAlt(STR_1);
        assertTrue(0 > route1.compareTo(route2));
    }

    @Test
    public void compareTo_ByAlt() {
        RouteVariableDto route1 = new RouteVariableDto().setMethod(METHOD1).setPath(STR_1).setAlt(STR_1);
        RouteVariableDto route2 = new RouteVariableDto().setMethod(METHOD1).setPath(STR_1).setAlt(STR_2);
        assertTrue(0 > route1.compareTo(route2));
    }

    @Test
    public void toString_EqualForEqualObjects() {
        RouteVariableDto route1 = new RouteVariableDto().setMethod(METHOD1).setPath(STR_1).setAlt(STR_1);
        RouteVariableDto route2 = new RouteVariableDto().setMethod(METHOD1).setPath(STR_1).setAlt(STR_1);
        assertEquals(route1.toString(), route2.toString());
    }
}
