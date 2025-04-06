package com.mockachu.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.junit.jupiter.api.Assertions.*;

class OutboundRequestTest {

    private static final RequestMethod METHOD1 = RequestMethod.GET;
    private static final RequestMethod METHOD2 = RequestMethod.PUT;
    private static final String STR_1 = "1";
    private static final String STR_2 = "2";

    @Test
    void setGroup_Null_GroupIsEmptyString() {
        assertTrue(new OutboundRequest().setGroup(null).getGroup().isEmpty());
    }

    @Test
    void setPath_Null_PathIsEmptyString() {
        assertTrue(new OutboundRequest().setPath(null).getPath().isEmpty());
    }

    @Test
    void setHeaders_Null_HeadersIsEmptyString() {
        assertTrue(new OutboundRequest().setHeaders(null).getHeaders().isEmpty());
    }

    @Test
    void setBody_Null_BodyIsEmptyString() {
        assertTrue(new OutboundRequest().setBody(null).getBody().isEmpty());
    }

    @Test
    void setTriggerRequestIds_Null_TriggerRequestIdsIsEmptyString() {
        assertTrue(new OutboundRequest().setTriggerRequestIds(null).getTriggerRequestIds().isEmpty());
    }

    @Test
    void setType_Null_TypeIsRest() {
        assertEquals(RouteType.REST, new OutboundRequest().setType(null).getType());
    }

    @DisplayName("Test Route equality based on method, path and alt (other parameters don't matter).")
    @Test
    void equals_SameId_OtherFieldsDiffer_True() {
        var var1 = new OutboundRequest()
                .setMethod(METHOD1)
                .setPath(STR_1)
                .setId(STR_1)
                .setGroup(STR_1)
                .setDisabled(false);
        var var2 = new OutboundRequest()
                .setMethod(METHOD1)
                .setPath(STR_1)
                .setId(STR_1)
                .setGroup(STR_2)
                .setDisabled(true);
        assertEquals(var1, var2);
    }

    @Test
    void equals_Null_False() {
        var v = new OutboundRequest().setMethod(METHOD1).setPath(STR_1).setId(STR_1);
        assertNotEquals(null, v);
    }

    @Test
    void equals_ObjectOfOtherType_False() {
        var v = new OutboundRequest().setMethod(METHOD1).setPath(STR_1).setId(STR_1);
        assertNotEquals(new Object(), v);
    }

    @Test
    void hashCode_EqualsForEqualObjects() {
        var v1 = new OutboundRequest().setMethod(METHOD1).setPath(STR_1).setId(STR_1);
        var v2 = new OutboundRequest().setMethod(METHOD1).setPath(STR_1).setId(STR_1);
        assertEquals(v1.hashCode(), v2.hashCode());
    }

    @Test
    void compareTo_Equal() {
        var v1 = new OutboundRequest().setMethod(METHOD1).setPath(STR_1).setId(STR_1);
        var v2 = new OutboundRequest().setMethod(METHOD1).setPath(STR_1).setId(STR_1);
        assertEquals(0, v1.compareTo(v2));
    }

    @Test
    void compareTo_ByGroup() {
        var v1 = new OutboundRequest()
                .setGroup(STR_1).setMethod(METHOD1).setPath(STR_1).setId(STR_1);
        var v2 = new OutboundRequest()
                .setGroup(STR_2).setMethod(METHOD1).setPath(STR_1).setId(STR_1);
        assertTrue(0 > v1.compareTo(v2));
    }

    @Test
    void compareTo_ByPath() {
        var v1 = new OutboundRequest()
                .setGroup(STR_1).setMethod(METHOD1).setPath(STR_1).setId(STR_1);
        var v2 = new OutboundRequest()
                .setGroup(STR_1).setMethod(METHOD1).setPath(STR_2).setId(STR_1);
        assertTrue(0 > v1.compareTo(v2));
    }

    @Test
    void compareAfterAssignFrom() {
        var v1 = new OutboundRequest()
                .setGroup(STR_1).setMethod(METHOD1).setPath(STR_1).setId(STR_1);
        var v2 = new OutboundRequest().assignFrom(v1);
        assertEquals(v1.getGroup(), v2.getGroup());
        assertEquals(v1.getMethod(), v2.getMethod());
        assertEquals(v1.getPath(), v2.getPath());
        assertEquals(v1.getId(), v2.getId());
    }

    @Test
    void correctIdGeneration() {
        var v = new OutboundRequest()
                .setMethod(RequestMethod.GET)
                .setPath("http://localhost:8081/test?id=5&data=${test}");
        assertEquals("GET.localhost.8081.test.id.5.data...test.", v.generateId());
    }

    @Test
    void correctToString() {
        var v = new OutboundRequest()
                .setMethod(RequestMethod.GET)
                .setPath("http://some-path");
        assertTrue(v.toString().contains("GET"));
        assertTrue(v.toString().contains("http://some-path"));
    }
}
