package com.mockservice.service;

import com.mockservice.domain.Route;
import com.mockservice.mapper.RouteMapper;
import com.mockservice.model.RouteDto;
import com.mockservice.model.RouteVariable;
import com.mockservice.model.RouteVariableDto;
import com.mockservice.repository.ConfigRepository;
import com.mockservice.template.MockVariables;
import com.mockservice.util.RandomUtils;
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
import java.util.function.BiConsumer;

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
    private static final String RESPONSE_WITH_VARIABLES = "${id} ... ${name:default} ...";

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
        Route route2 = new Route().setMethod(METHOD).setPath(PATH).setAlt("var1 = \"tes value\"");
        Route route3 = new Route().setMethod(METHOD).setPath(PATH).setAlt("var1 = \"test value\"");
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
        when(routeMapper.toDto(anyList(), any())).thenReturn(List.of(routeDto));

        RouteService service = service();

        List<RouteDto> routes = service.getRoutes();
        assertTrue(routes.contains(routeDto));
    }

    @Test
    void getRoutes_RouteWithVariablesExistsAndVariableValueExists_ReturnsVariables() {
        Route route = new Route().setPath(PATH).setResponse(RESPONSE_WITH_VARIABLES);
        when(configRepository.findAllRoutes()).thenReturn(List.of(route));
        when(configRepository.getRouteVariables(any())).thenReturn(List.of(
                new RouteVariable().setName("id"),
                new RouteVariable().setName("name").setDefaultValue("default")
        ));

        RouteDto routeDto = new RouteDto().setPath(PATH).setResponse(RESPONSE_WITH_VARIABLES);
        doAnswer(ans -> {
            BiConsumer<Route, RouteDto> postProcess = (BiConsumer<Route, RouteDto>) ans.getArguments()[1];
            postProcess.accept(route, routeDto);
            return List.of(routeDto);
        }).when(routeMapper).toDto(anyList(), any());

        RouteService service = service();
        service.setRouteVariable(new RouteVariableDto().setPath(PATH).setName("id").setValue("123"));
        List<RouteDto> routes = service.getRoutes();

        assertEquals(1, routes.size());
        assertEquals(2, routes.get(0).getVariables().size());

        RouteVariable var0 = routes.get(0).getVariables().get(0);
        RouteVariable var1 = routes.get(0).getVariables().get(1);
        assertAll(() -> {
            assertEquals("id", var0.getName());
            assertNull(var0.getDefaultValue());
            assertEquals("123", var0.getValue());

            assertEquals("name", var1.getName());
            assertEquals("default", var1.getDefaultValue());
            assertNull(var1.getValue());
        });
    }

    @Test
    void getRoutes_RouteWithVariablesExistsAndVariableValueDoesNotExist_ReturnsDefaults() {
        Route route = new Route().setPath(PATH).setResponse(RESPONSE_WITH_VARIABLES);
        when(configRepository.findAllRoutes()).thenReturn(List.of(route));
        when(configRepository.getRouteVariables(any())).thenReturn(List.of(
                new RouteVariable().setName("id"),
                new RouteVariable().setName("name").setDefaultValue("default")
        ));

        RouteDto routeDto = new RouteDto().setPath(PATH).setResponse(RESPONSE_WITH_VARIABLES);
        doAnswer(ans -> {
            BiConsumer<Route, RouteDto> postProcess = (BiConsumer<Route, RouteDto>) ans.getArguments()[1];
            postProcess.accept(route, routeDto);
            return List.of(routeDto);
        }).when(routeMapper).toDto(anyList(), any());

        RouteService service = service();
        List<RouteDto> routes = service.getRoutes();

        assertEquals(1, routes.size());
        assertEquals(2, routes.get(0).getVariables().size());

        RouteVariable var0 = routes.get(0).getVariables().get(0);
        RouteVariable var1 = routes.get(0).getVariables().get(1);
        assertAll(() -> {
            assertEquals("id", var0.getName());
            assertNull(var0.getDefaultValue());
            assertNull(var0.getValue());

            assertEquals("name", var1.getName());
            assertEquals("default", var1.getDefaultValue());
            assertNull(var1.getValue());
        });
    }

    @Test
    void getRoutes_RouteWithFunctionsExists_ReturnsNoVariables() {
        Route route = new Route().setPath(PATH).setResponse(RESPONSE_WITH_VARIABLES);
        when(configRepository.findAllRoutes()).thenReturn(List.of(route));

        RouteDto routeDto = new RouteDto().setPath(PATH).setResponse(RESPONSE_WITH_VARIABLES);
        doAnswer(ans -> {
            BiConsumer<Route, RouteDto> postProcess = (BiConsumer<Route, RouteDto>) ans.getArguments()[1];
            postProcess.accept(route, routeDto);
            return List.of(routeDto);
        }).when(routeMapper).toDto(anyList(), any());

        RouteService service = service();
        service.setRouteVariable(new RouteVariableDto().setPath(PATH).setName("id").setValue("123"));
        List<RouteDto> routes = service.getRoutes();

        assertEquals(1, routes.size());
        assertTrue(routes.get(0).getVariables().isEmpty());
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

    @Test
    void setRouteVariable_OneVariable_VariableCreated() {
        RouteService service = service();
        service.setRouteVariable(new RouteVariableDto().setPath(PATH).setName("id").setValue("123"));
        Route route = new Route().setPath(PATH);
        MockVariables routeVariables = service.getRouteVariables(route);

        assertEquals(1, routeVariables.size());
        assertEquals("123", routeVariables.get("id"));
    }

    @Test
    void clearRouteVariable_ClearExisting_VariableDoesNotExist() {
        RouteService service = service();
        service.setRouteVariable(new RouteVariableDto().setPath(PATH).setName("id").setValue("123"));
        service.clearRouteVariable(new RouteVariableDto().setPath(PATH).setName("id"));
        Route route = new Route().setPath(PATH);
        MockVariables routeVariables = service.getRouteVariables(route);

        assertNull(routeVariables);
    }

    @Test
    void clearRouteVariable_ClearNonExisting_VariableDoesNotExist() {
        RouteService service = service();
        service.setRouteVariable(new RouteVariableDto().setPath(PATH).setName("id").setValue("123"));
        service.clearRouteVariable(new RouteVariableDto().setPath(PATH).setName("name"));
        Route route = new Route().setPath(PATH);
        MockVariables routeVariables = service.getRouteVariables(route);

        assertFalse(routeVariables.isEmpty());
    }

    @Test
    void clearRouteVariable_ClearForNonExistingRoute_VariableExists() {
        RouteService service = service();
        service.setRouteVariable(new RouteVariableDto().setPath(PATH).setName("id").setValue("123"));
        service.clearRouteVariable(new RouteVariableDto().setPath(PATH).setMethod(METHOD).setAlt(ALT1).setName("id"));
        Route route = new Route().setPath(PATH);
        MockVariables routeVariables = service.getRouteVariables(route);

        assertEquals(1, routeVariables.size());
        assertEquals("123", routeVariables.get("id"));
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
