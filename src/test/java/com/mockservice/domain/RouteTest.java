package com.mockservice.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMethod;

public class RouteTest {

    private static final RequestMethod METHOD1 = RequestMethod.GET;
    private static final RequestMethod METHOD2 = RequestMethod.PUT;

    private static final String STR_1 = "1";
    private static final String STR_2 = "2";


    @Test
    public void setGroup_Null_GroupIsEmptyString() {
        assertTrue(new Route().setGroup(null).getGroup().isEmpty());
    }

    @Test
    public void setPath_Null_PathIsEmptyString() {
        assertTrue(new Route().setPath(null).getPath().isEmpty());
    }

    @Test
    public void setAlt_Null_AltIsEmptyString() {
        assertTrue(new Route().setAlt(null).getAlt().isEmpty());
    }

    @Test
    public void setResponse_Null_ResponseIsEmptyString() {
        assertTrue(new Route().setResponse(null).getResponse().isEmpty());
    }

    @Test
    public void setRequestBodySchema_Null_RequestBodySchemaIsEmptyString() {
        assertTrue(new Route().setRequestBodySchema(null).getRequestBodySchema().isEmpty());
    }

    @Test
    public void setType_Null_TypeIsRest() {
        assertTrue(new Route().setType(null).isRest());
    }

    @Test
    public void setResponseCodeString_In100To599_SetsResponseCode() {
        assertEquals(400, new Route().setResponseCodeString("400").getResponseCode());
    }

    @Test
    public void setResponseCodeString_LessThan100_DoesNotChangeResponseCode() {
        assertEquals(new Route().getResponseCode(), new Route().setResponseCodeString("99").getResponseCode());
    }

    @Test
    public void setResponseCodeString_GreaterThan599_DoesNotChangeResponseCode() {
        assertEquals(new Route().getResponseCode(), new Route().setResponseCodeString("600").getResponseCode());
    }

    @DisplayName("Test Route equality based on method, path and alt (other parameters don't matter).")
    @Test
    public void equals_SameMethodPathAlt_OtherFieldsDiffer_True() {
        Route route1 = new Route().setMethod(METHOD1).setPath(STR_1).setAlt(STR_1)
                .setGroup(STR_1)
                .setResponse(STR_1)
                .setRequestBodySchema(STR_1)
                .setResponseCode(200)
                .setDisabled(false);
        Route route2 = new Route().setMethod(METHOD1).setPath(STR_1).setAlt(STR_1)
                .setGroup(STR_2)
                .setResponse(STR_2)
                .setRequestBodySchema(STR_2)
                .setResponseCode(400)
                .setDisabled(true);
        assertEquals(route1, route2);
    }

    @Test
    public void equals_Null_False() {
        Route route = new Route().setMethod(METHOD1).setPath(STR_1).setAlt(STR_1);
        assertNotEquals(null, route);
    }

    @Test
    public void equals_ObjectOfOtherType_False() {
        Route route = new Route().setMethod(METHOD1).setPath(STR_1).setAlt(STR_1);
        assertNotEquals(route, new Object());
    }

    @Test
    public void equals_DifferentMethod_False() {
        Route route1 = new Route().setMethod(METHOD1).setPath(STR_1).setAlt(STR_1);
        Route route2 = new Route(route1).setMethod(METHOD2);
        assertNotEquals(route1, route2);
    }

    @Test
    public void equals_DifferentPath_False() {
        Route route1 = new Route().setMethod(METHOD1).setPath(STR_1).setAlt(STR_1);
        Route route2 = new Route(route1).setPath(STR_2);
        assertNotEquals(route1, route2);
    }

    @Test
    public void equals_DifferentAlt_False() {
        Route route1 = new Route().setMethod(METHOD1).setPath(STR_1).setAlt(STR_1);
        Route route2 = new Route(route1).setAlt(STR_2);
        assertNotEquals(route1, route2);
    }

    @Test
    public void compareTo_Equal() {
        Route route1 = new Route()
                .setGroup(STR_1).setType(RouteType.REST).setMethod(METHOD1).setPath(STR_1).setAlt(STR_1);
        Route route2 = new Route()
                .setGroup(STR_1).setType(RouteType.REST).setMethod(METHOD1).setPath(STR_1).setAlt(STR_1);
        assertEquals(0, route1.compareTo(route2));
    }

    @Test
    public void compareTo_ByGroup() {
        Route route1 = new Route()
                .setGroup(STR_1).setType(RouteType.REST).setMethod(METHOD1).setPath(STR_1).setAlt(STR_1);
        Route route2 = new Route()
                .setGroup(STR_2).setType(RouteType.REST).setMethod(METHOD1).setPath(STR_1).setAlt(STR_1);
        assertTrue(0 > route1.compareTo(route2));
    }

    @Test
    public void compareTo_ByType() {
        Route route1 = new Route()
                .setGroup(STR_1).setType(RouteType.REST).setMethod(METHOD1).setPath(STR_1).setAlt(STR_1);
        Route route2 = new Route()
                .setGroup(STR_1).setType(RouteType.SOAP).setMethod(METHOD1).setPath(STR_1).setAlt(STR_1);
        assertTrue(0 > route1.compareTo(route2));
    }

    @Test
    public void compareTo_ByMethod() {
        Route route1 = new Route()
                .setGroup(STR_1).setType(RouteType.REST).setMethod(METHOD1).setPath(STR_1).setAlt(STR_1);
        Route route2 = new Route()
                .setGroup(STR_1).setType(RouteType.REST).setMethod(METHOD2).setPath(STR_1).setAlt(STR_1);
        assertTrue(0 > route1.compareTo(route2));
    }
    @Test
    public void compareTo_ByPath() {
        Route route1 = new Route()
                .setGroup(STR_1).setType(RouteType.REST).setMethod(METHOD1).setPath(STR_1).setAlt(STR_1);
        Route route2 = new Route()
                .setGroup(STR_1).setType(RouteType.REST).setMethod(METHOD1).setPath(STR_2).setAlt(STR_1);
        assertTrue(0 > route1.compareTo(route2));
    }
    @Test
    public void compareTo_ByAlt() {
        Route route1 = new Route()
                .setGroup(STR_1).setType(RouteType.REST).setMethod(METHOD1).setPath(STR_1).setAlt(STR_1);
        Route route2 = new Route()
                .setGroup(STR_1).setType(RouteType.REST).setMethod(METHOD1).setPath(STR_1).setAlt(STR_2);
        assertTrue(0 > route1.compareTo(route2));
    }
}
