package com.mockachu.service;

import com.mockachu.domain.Route;
import com.mockachu.mapper.RouteMapper;
import com.mockachu.model.RouteDto;
import com.mockachu.repository.ConfigRepository;
import com.mockachu.template.MockVariables;
import com.mockachu.util.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class RouteServiceImplTest {

    private static final RequestMethod METHOD = RequestMethod.POST;
    private static final RequestMethod METHOD_OTHER = RequestMethod.PUT;
    private static final String PATH = "/test";
    private static final String PATH_OTHER = "/api/test";
    private static final String ALT1 = "400";
    private static final String ALT2 = "204";

    @Mock
    private ConfigRepository configRepository;
    @Mock
    private RouteMapper routeMapper;
    @Mock
    private RandomUtils randomUtils;

    private RouteService service() {
        return new RouteServiceImpl(configRepository, routeMapper, randomUtils);
    }

    @Test
    void getEnabledRoute_EnabledRouteExists_ReturnsRoute() {
        Route route = new Route().setPath(PATH);
        when(configRepository.findRoute(any())).thenReturn(Optional.of(route));

        RouteService service = service();
        Route key = new Route().setPath(PATH);

        assertTrue(service.getEnabledRoute(key).isPresent());
    }

    @Test
    void getEnabledRoute_DisabledRouteExists_ReturnsEmpty() {
        Route route = new Route().setPath(PATH).setDisabled(true);
        when(configRepository.findRoute(any())).thenReturn(Optional.of(route));

        RouteService service = service();
        Route key = new Route().setPath(PATH);

        assertTrue(service.getEnabledRoute(key).isEmpty());
    }

    @Test
    void getRouteForVariables_RoutesWithMatchingCondition_ReturnsCorrectRoute() {
        Route route1 = new Route().setMethod(METHOD).setPath(PATH).setAlt("1");
        Route route2 = new Route().setMethod(METHOD).setPath(PATH).setAlt("var1 = tes value");
        Route route3 = new Route().setMethod(METHOD).setPath(PATH).setAlt("var1 = test value");
        when(configRepository.findAllRoutes()).thenReturn(List.of(route1, route2, route3));

        RouteService service = service();
        MockVariables variables = new MockVariables().put("var1", "test value");
        Optional<Route> route = service.getRouteForVariables(METHOD, PATH, variables);

        assertTrue(route.isPresent());
        assertEquals(route3, route.get());
    }

    @Test
    void getRouteForVariables_RoutesWithNonMatchingCondition_ReturnsEmpty() {
        Route route1 = new Route().setMethod(METHOD).setPath(PATH).setAlt("1");
        Route route2 = new Route().setMethod(METHOD).setPath(PATH).setAlt("var1 = \"tes value\"");
        Route route3 = new Route().setMethod(METHOD).setPath(PATH).setAlt("var1 = \"test value\"");
        when(configRepository.findAllRoutes()).thenReturn(List.of(route1, route2, route3));

        RouteService service = service();
        MockVariables variables = new MockVariables().put("var2", "test value");
        Optional<Route> route = service.getRouteForVariables(METHOD, PATH, variables);

        assertFalse(route.isPresent());
    }

    @Test
    void getRouteForVariables_ConditionHasBeenReset_ReturnsEmpty() {
        Route route3 = new Route().setMethod(METHOD).setPath(PATH).setAlt("var1 = \"test value\"");
        route3.setAlt("");
        when(configRepository.findAllRoutes()).thenReturn(List.of(route3));

        RouteService service = service();
        MockVariables variables = new MockVariables().put("var1", "test value");
        Optional<Route> route = service.getRouteForVariables(METHOD, PATH, variables);

        assertFalse(route.isPresent());
    }

    @Test
    void getRoutes_RouteExists_ReturnsRouteDto() {
        Route route = new Route().setPath(PATH);
        when(configRepository.findAllRoutes()).thenReturn(List.of(route));
        RouteDto routeDto = new RouteDto().setPath(PATH);
        when(routeMapper.toDto(anyList())).thenReturn(List.of(routeDto));

        RouteService service = service();

        List<RouteDto> routes = service.getRoutes();
        assertTrue(routes.contains(routeDto));
    }

    @Test
    void putRoute_RouteDtoAsInput_CallsRepositoryMethod() throws IOException {
        RouteService service = service();
        service.putRoute(new RouteDto(), new RouteDto());

        verify(configRepository, times(1)).putRoute(any(), any());
    }

    @Test
    void putRoutes_ListOfRouteDtoAsInput_CallsRepositoryMethod() throws IOException {
        RouteService service = service();
        service.putRoutes(List.of(new RouteDto()), true);

        verify(configRepository, times(1)).putRoutes(anyList(), anyBoolean());
    }

    @Test
    void deleteRoutes_ListOfRouteDtoAsInput_CallsRepositoryMethod() throws IOException {
        Route route = new Route().setPath(PATH);
        when(routeMapper.fromDto(anyList())).thenReturn(List.of(route));

        RouteService service = service();
        service.deleteRoutes(List.of(new RouteDto().setPath(PATH)));

        ArgumentCaptor<List<Route>> routeListCaptor = ArgumentCaptor.forClass(List.class);
        verify(configRepository).deleteRoutes(routeListCaptor.capture());
        assertFalse(routeListCaptor.getValue().isEmpty());
        assertEquals(PATH, routeListCaptor.getValue().get(0).getPath());
    }

    // --- random alt -----------------------------------------------------

    @Test
    void getRandomAltFor_MoreThanOneRouteSatisfiesSearchCondition_ReturnsAltOfEither() {
        Route route1 = new Route().setMethod(METHOD).setPath(PATH).setAlt(ALT1);
        Route route2 = new Route().setMethod(METHOD).setPath(PATH).setAlt(ALT2);
        when(configRepository.findAllRoutes()).thenReturn(List.of(route1, route2));

        RouteService service = service();

        Optional<String> alt = service.getRandomAltFor(METHOD, PATH);
        assertTrue(alt.isPresent());
        assertTrue(Set.of(ALT1, ALT2).contains(alt.get()));
    }

    @Test
    void getRandomAltFor_OneRouteSatisfiesSearchCondition_ReturnsAltOfTheRoute() {
        Route route1 = new Route().setMethod(METHOD).setPath(PATH).setAlt(ALT1);
        when(configRepository.findAllRoutes()).thenReturn(List.of(route1));

        RouteService service = service();

        Optional<String> alt = service.getRandomAltFor(METHOD, PATH);
        assertTrue(alt.isPresent());
        assertEquals(ALT1, alt.get());
    }

    @Test
    void getRandomAltFor_NoRoutesSatisfySearchConditionByMethod_ReturnsEmpty() {
        Route route1 = new Route().setMethod(METHOD).setPath(PATH).setAlt(ALT1);
        when(configRepository.findAllRoutes()).thenReturn(List.of(route1));

        RouteService service = service();

        assertTrue(service.getRandomAltFor(METHOD_OTHER, PATH).isEmpty());
    }

    @Test
    void getRandomAltFor_NoRoutesSatisfySearchConditionByPath_ReturnsEmpty() {
        Route route1 = new Route().setMethod(METHOD).setPath(PATH).setAlt(ALT1);
        Route route2 = new Route().setMethod(METHOD).setPath(PATH).setAlt(ALT2);
        when(configRepository.findAllRoutes()).thenReturn(List.of(route1, route2));

        RouteService service = service();

        assertTrue(service.getRandomAltFor(METHOD, PATH_OTHER).isEmpty());
    }

    @Test
    void getRandomAltFor_AnyNumberOfRoutesSatisfySearchConditionButAreDisabled_ReturnsEmpty() {
        Route route1 = new Route().setMethod(METHOD).setPath(PATH).setAlt(ALT1).setDisabled(true);
        Route route2 = new Route().setMethod(METHOD).setPath(PATH).setAlt(ALT2).setDisabled(true);
        when(configRepository.findAllRoutes()).thenReturn(List.of(route1, route2));

        RouteService service = service();

        assertTrue(service.getRandomAltFor(METHOD, PATH).isEmpty());
    }
}
