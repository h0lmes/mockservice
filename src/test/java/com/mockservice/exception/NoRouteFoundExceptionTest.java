package com.mockservice.exception;

import com.mockservice.domain.Route;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NoRouteFoundExceptionTest {

    private static final RequestMethod METHOD = RequestMethod.GET;
    private static final String PATH = "/api/v1/test";
    private static final String ALT_400 = "400";

    @Test
    public void toString_RouteNotNull_EqualsRouteToString() {
        Route route = new Route().setMethod(METHOD).setPath(PATH).setAlt(ALT_400);
        NoRouteFoundException exception = new NoRouteFoundException(route);
        assertEquals(route.toString(), exception.toString());
    }

    @Test
    public void toString_RouteIsNull_EqualsEmptyString() {
        NoRouteFoundException exception = new NoRouteFoundException(null);
        assertEquals("", exception.toString());
    }
}
