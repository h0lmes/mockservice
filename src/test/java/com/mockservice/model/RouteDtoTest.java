package com.mockservice.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class RouteDtoTest {

    private static final RequestMethod METHOD1 = RequestMethod.GET;
    private static final RequestMethod METHOD2 = RequestMethod.PUT;

    private static final String STR_1 = "1";
    private static final String STR_2 = "2";

    @DisplayName("Test equality based on method, path and alt (other parameters don't matter).")
    @Test
    void equals_SameMethodPathAlt_OtherFieldsDiffer_True() {
        RouteDto route1 = new RouteDto().setMethod(METHOD1).setPath(STR_1).setAlt(STR_1)
                .setGroup(STR_1)
                .setResponse(STR_1)
                .setRequestBodySchema(STR_1)
                .setResponseCode(200)
                .setDisabled(false);
        RouteDto route2 = new RouteDto().setMethod(METHOD1).setPath(STR_1).setAlt(STR_1)
                .setGroup(STR_2)
                .setResponse(STR_2)
                .setRequestBodySchema(STR_2)
                .setResponseCode(400)
                .setDisabled(true);
        assertEquals(route1, route2);
    }

    @Test
    void equals_Null_False() {
        RouteDto route = new RouteDto().setMethod(METHOD1).setPath(STR_1).setAlt(STR_1);
        assertNotEquals(null, route);
    }

    @Test
    void equals_ObjectOfOtherType_False() {
        RouteDto route = new RouteDto().setMethod(METHOD1).setPath(STR_1).setAlt(STR_1);
        assertNotEquals(route, new Object());
    }

    @Test
    void equals_DifferentMethod_False() {
        RouteDto route1 = new RouteDto().setMethod(METHOD1).setPath(STR_1).setAlt(STR_1);
        RouteDto route2 = new RouteDto().setMethod(METHOD2).setPath(STR_1).setAlt(STR_1);
        assertNotEquals(route1, route2);
    }

    @Test
    void equals_DifferentPath_False() {
        RouteDto route1 = new RouteDto().setMethod(METHOD1).setPath(STR_1).setAlt(STR_1);
        RouteDto route2 = new RouteDto().setMethod(METHOD1).setPath(STR_2).setAlt(STR_1);
        assertNotEquals(route1, route2);
    }

    @Test
    void equals_DifferentAlt_False() {
        RouteDto route1 = new RouteDto().setMethod(METHOD1).setPath(STR_1).setAlt(STR_1);
        RouteDto route2 = new RouteDto().setMethod(METHOD1).setPath(STR_1).setAlt(STR_2);
        assertNotEquals(route1, route2);
    }

    @Test
    void hashCode_EqualsForEqualObjects() {
        RouteDto route1 = new RouteDto().setMethod(METHOD1).setPath(STR_1).setAlt(STR_1);
        RouteDto route2 = new RouteDto().setMethod(METHOD1).setPath(STR_1).setAlt(STR_1);
        assertEquals(route1.hashCode(), route2.hashCode());
    }

    @Test
    void toString_EqualsForEqualObjects() {
        RouteDto route1 = new RouteDto().setMethod(METHOD1).setPath(STR_1).setAlt(STR_1);
        RouteDto route2 = new RouteDto().setMethod(METHOD1).setPath(STR_1).setAlt(STR_1);
        assertEquals(route1.toString(), route2.toString());
    }
}
