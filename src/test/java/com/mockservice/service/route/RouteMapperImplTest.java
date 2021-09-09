package com.mockservice.service.route;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mockservice.domain.Route;
import com.mockservice.domain.RouteType;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.annotation.RequestMethod;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class RouteMapperImplTest {

    private static final RequestMethod METHOD = RequestMethod.POST;
    private static final String STR = "AAA";
    private static final int INT = 404;

    private Route route() {
        return new Route()
            .setId(STR)
            .setGroup(STR)
            .setType(RouteType.SOAP)
            .setMethod(METHOD)
            .setPath(STR)
            .setAlt(STR)
            .setResponseCode(INT)
            .setResponse(STR)
            .setRequestBodySchema(STR)
            .setDisabled(true);
    }

    private RouteDto routeDto() {
        return new RouteDto()
            .setId(STR)
            .setGroup(STR)
            .setType(RouteType.SOAP)
            .setMethod(METHOD)
            .setPath(STR)
            .setAlt(STR)
            .setResponseCode(INT)
            .setResponse(STR)
            .setRequestBodySchema(STR)
            .setDisabled(true);
    }

    @Test
    public void toDto_OneInput_MappedCorrect() {
        RouteMapper mapper = new RouteMapperImpl();
        Route route = route();
        RouteDto routeDto = mapper.toDto(route, null);

        assertAll(() -> {
            assertEquals(route.getId(), routeDto.getId());
            assertEquals(route.getGroup(), routeDto.getGroup());
            assertEquals(route.getType(), routeDto.getType());
            assertEquals(route.getMethod(), routeDto.getMethod());
            assertEquals(route.getPath(), routeDto.getPath());
            assertEquals(route.getAlt(), routeDto.getAlt());
            assertEquals(route.getResponseCode(), routeDto.getResponseCode());
            assertEquals(route.getResponse(), routeDto.getResponse());
            assertEquals(route.getRequestBodySchema(), routeDto.getRequestBodySchema());
            assertEquals(route.getDisabled(), routeDto.getDisabled());
        });
    }

    @Test
    public void toDto_OneInputWithPostProcess_CallsPostProcess() {
        RouteMapper mapper = new RouteMapperImpl();
        Route route = route();
        Route[] routes = new Route[1];
        RouteDto[] dtos = new RouteDto[1];
        mapper.toDto(route, (r, d) -> {
            routes[0] = r;
            dtos[0] = d;
        });
        RouteDto routeDto = dtos[0];

        assertAll(() -> {
            assertEquals(route, routes[0]);
            assertEquals(route.getId(), routeDto.getId());
            assertEquals(route.getGroup(), routeDto.getGroup());
            assertEquals(route.getType(), routeDto.getType());
            assertEquals(route.getMethod(), routeDto.getMethod());
            assertEquals(route.getPath(), routeDto.getPath());
            assertEquals(route.getAlt(), routeDto.getAlt());
            assertEquals(route.getResponseCode(), routeDto.getResponseCode());
            assertEquals(route.getResponse(), routeDto.getResponse());
            assertEquals(route.getRequestBodySchema(), routeDto.getRequestBodySchema());
            assertEquals(route.getDisabled(), routeDto.getDisabled());
        });
    }

    @Test
    public void toDto_ListInput_MappedCorrect() {
        RouteMapper mapper = new RouteMapperImpl();
        Route route = route();
        List<RouteDto> routeDtos = mapper.toDto(List.of(route), null);

        assertEquals(1, routeDtos.size());

        RouteDto routeDto = routeDtos.get(0);
        assertAll(() -> {
            assertEquals(route.getId(), routeDto.getId());
            assertEquals(route.getGroup(), routeDto.getGroup());
            assertEquals(route.getType(), routeDto.getType());
            assertEquals(route.getMethod(), routeDto.getMethod());
            assertEquals(route.getPath(), routeDto.getPath());
            assertEquals(route.getAlt(), routeDto.getAlt());
            assertEquals(route.getResponseCode(), routeDto.getResponseCode());
            assertEquals(route.getResponse(), routeDto.getResponse());
            assertEquals(route.getRequestBodySchema(), routeDto.getRequestBodySchema());
            assertEquals(route.getDisabled(), routeDto.getDisabled());
        });
    }

    @Test
    public void toDto_ListInputWithPostProcess_CallsPostProcess() {
        RouteMapper mapper = new RouteMapperImpl();
        Route route = route();
        Route[] routes = new Route[1];
        RouteDto[] dtos = new RouteDto[1];
        mapper.toDto(List.of(route), (r, d) -> {
            routes[0] = r;
            dtos[0] = d;
        });
        RouteDto routeDto = dtos[0];

        assertAll(() -> {
            assertEquals(route, routes[0]);
            assertEquals(route.getId(), routeDto.getId());
            assertEquals(route.getGroup(), routeDto.getGroup());
            assertEquals(route.getType(), routeDto.getType());
            assertEquals(route.getMethod(), routeDto.getMethod());
            assertEquals(route.getPath(), routeDto.getPath());
            assertEquals(route.getAlt(), routeDto.getAlt());
            assertEquals(route.getResponseCode(), routeDto.getResponseCode());
            assertEquals(route.getResponse(), routeDto.getResponse());
            assertEquals(route.getRequestBodySchema(), routeDto.getRequestBodySchema());
            assertEquals(route.getDisabled(), routeDto.getDisabled());
        });
    }

    @Test
    public void fromDto_OneInput_MappedCorrect() {
        RouteMapper mapper = new RouteMapperImpl();
        RouteDto routeDto = routeDto();
        Route route = mapper.fromDto(routeDto);

        assertAll(() -> {
            assertEquals(routeDto.getId(), route.getId());
            assertEquals(routeDto.getGroup(), route.getGroup());
            assertEquals(routeDto.getType(), route.getType());
            assertEquals(routeDto.getMethod(), route.getMethod());
            assertEquals(routeDto.getPath(), route.getPath());
            assertEquals(routeDto.getAlt(), route.getAlt());
            assertEquals(routeDto.getResponseCode(), route.getResponseCode());
            assertEquals(routeDto.getResponse(), route.getResponse());
            assertEquals(routeDto.getRequestBodySchema(), route.getRequestBodySchema());
            assertEquals(routeDto.getDisabled(), route.getDisabled());
        });
    }

    @Test
    public void fromDto_ListInput_MappedCorrect() {
        RouteMapper mapper = new RouteMapperImpl();
        RouteDto routeDto = routeDto();
        List<Route> routes = mapper.fromDto(List.of(routeDto));

        assertEquals(1, routes.size());

        Route route = routes.get(0);
        assertAll(() -> {
            assertEquals(routeDto.getId(), route.getId());
            assertEquals(routeDto.getGroup(), route.getGroup());
            assertEquals(routeDto.getType(), route.getType());
            assertEquals(routeDto.getMethod(), route.getMethod());
            assertEquals(routeDto.getPath(), route.getPath());
            assertEquals(routeDto.getAlt(), route.getAlt());
            assertEquals(routeDto.getResponseCode(), route.getResponseCode());
            assertEquals(routeDto.getResponse(), route.getResponse());
            assertEquals(routeDto.getRequestBodySchema(), route.getRequestBodySchema());
            assertEquals(routeDto.getDisabled(), route.getDisabled());
        });
    }
}
