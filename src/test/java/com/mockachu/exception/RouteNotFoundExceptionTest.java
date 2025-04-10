package com.mockachu.exception;

import com.mockachu.domain.Route;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RouteNotFoundExceptionTest {

    private static final RequestMethod METHOD = RequestMethod.GET;
    private static final String PATH = "/api/v1/test";
    private static final String ALT_400 = "400";

    @Test
    public void toString_RouteNotNull_EqualsRouteToString() {
        Route route = new Route().setMethod(METHOD).setPath(PATH).setAlt(ALT_400);
        RouteNotFoundException exception = new RouteNotFoundException(route);
        assertEquals(route.toString(), exception.toString());
    }

    @Test
    public void toString_RouteIsNull_EqualsEmptyString() {
        RouteNotFoundException exception = new RouteNotFoundException(null);
        assertEquals("", exception.toString());
    }
}
