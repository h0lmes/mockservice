package com.mockservice.service.route;

import com.mockservice.domain.Route;
import com.mockservice.repository.ConfigRepository;
import com.mockservice.template.MockVariables;
import com.mockservice.util.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
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

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class RouteServiceImplTest {

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
    public void getEnabledRoute_EnabledRouteExists_ReturnsRoute() {
        Route route = new Route().setPath(PATH);
        when(configRepository.findRoute(any())).thenReturn(Optional.of(route));

        RouteService service = service();
        Route key = new Route().setPath(PATH);

        assertTrue(service.getEnabledRoute(key).isPresent());
    }

    @Test
    public void getEnabledRoute_DisabledRouteExists_ReturnsEmpty() {
        Route route = new Route().setPath(PATH).setDisabled(true);
        when(configRepository.findRoute(any())).thenReturn(Optional.of(route));

        RouteService service = service();
        Route key = new Route().setPath(PATH);

        assertTrue(service.getEnabledRoute(key).isEmpty());
    }

    @Test
    public void getRoutes_RouteExists_ReturnsRouteDto() {
        Route route = new Route().setPath(PATH);
        when(configRepository.findAllRoutes()).thenReturn(List.of(route));
        RouteDto routeDto = new RouteDto().setPath(PATH);
        when(routeMapper.toDto(anyList(), any())).thenReturn(List.of(routeDto));

        RouteService service = service();

        List<RouteDto> routes = service.getRoutes();
        assertTrue(routes.contains(routeDto));
    }

    @Test
    public void getRoutes_RouteWithVariablesExistsAndVariableValueExists_ReturnsVariables() {
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
    public void getRoutes_RouteWithVariablesExistsAndVariableValueDoesNotExist_ReturnsDefaults() {
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
    public void getRoutes_RouteWithFunctionsExists_ReturnsNoVariables() {
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
    public void putRoute_RouteDtoAsInput_CallsRepositoryMethod() throws IOException {
        RouteService service = service();
        service.putRoute(new RouteDto(), new RouteDto());

        verify(configRepository, times(1)).putRoute(any(), any());
    }

    @Test
    public void putRoutes_ListOfRouteDtoAsInput_CallsRepositoryMethod() throws IOException {
        RouteService service = service();
        service.putRoutes(List.of(new RouteDto()), true);

        verify(configRepository, times(1)).putRoutes(anyList(), anyBoolean());
    }

    @Test
    public void deleteRoutes_ListOfRouteDtoAsInput_CallsRepositoryMethod() throws IOException {
        Route route = new Route().setPath(PATH);
        when(routeMapper.fromDto(anyList())).thenReturn(List.of(route));

        RouteService service = service();
        service.deleteRoutes(List.of(new RouteDto().setPath(PATH)));

        ArgumentCaptor<List<Route>> argument = ArgumentCaptor.forClass(List.class);
        verify(configRepository).deleteRoutes(argument.capture());
        assertFalse(argument.getValue().isEmpty());
        assertEquals(PATH, argument.getValue().get(0).getPath());
    }

    @Test
    public void setRouteVariable_OneVariable_VariableCreated() throws IOException {
        RouteService service = service();
        service.setRouteVariable(new RouteVariableDto().setPath(PATH).setName("id").setValue("123"));
        Route route = new Route().setPath(PATH);
        MockVariables routeVariables = service.getRouteVariables(route);

        assertEquals(1, routeVariables.size());
        assertEquals("123", routeVariables.get("id"));
    }

    @Test
    public void clearRouteVariable_ClearExisting_VariableDoesNotExist() throws IOException {
        RouteService service = service();
        service.setRouteVariable(new RouteVariableDto().setPath(PATH).setName("id").setValue("123"));
        service.clearRouteVariable(new RouteVariableDto().setPath(PATH).setName("id"));
        Route route = new Route().setPath(PATH);
        MockVariables routeVariables = service.getRouteVariables(route);

        assertNull(routeVariables);
    }

    @Test
    public void clearRouteVariable_ClearNonExisting_VariableDoesNotExist() throws IOException {
        RouteService service = service();
        service.setRouteVariable(new RouteVariableDto().setPath(PATH).setName("id").setValue("123"));
        service.clearRouteVariable(new RouteVariableDto().setPath(PATH).setName("name"));
        Route route = new Route().setPath(PATH);
        MockVariables routeVariables = service.getRouteVariables(route);

        assertFalse(routeVariables.isEmpty());
    }

    @Test
    public void clearRouteVariable_ClearForNonExistingRoute_VariableExists() throws IOException {
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
    public void getRandomAltFor_MoreThanOneRouteSatisfiesSearchCondition_ReturnsAltOfEither() {
        Route route1 = new Route().setMethod(METHOD).setPath(PATH).setAlt(ALT1);
        Route route2 = new Route().setMethod(METHOD).setPath(PATH).setAlt(ALT2);
        when(configRepository.findAllRoutes()).thenReturn(List.of(route1, route2));

        RouteService service = service();

        Optional<String> alt = service.getRandomAltFor(METHOD, PATH);
        assertTrue(alt.isPresent());
        assertTrue(Set.of(ALT1, ALT2).contains(alt.get()));
    }

    @Test
    public void getRandomAltFor_OneRouteSatisfiesSearchCondition_ReturnsAltOfTheRoute() {
        Route route1 = new Route().setMethod(METHOD).setPath(PATH).setAlt(ALT1);
        when(configRepository.findAllRoutes()).thenReturn(List.of(route1));

        RouteService service = service();

        Optional<String> alt = service.getRandomAltFor(METHOD, PATH);
        assertTrue(alt.isPresent());
        assertEquals(ALT1, alt.get());
    }

    @Test
    public void getRandomAltFor_NoRoutesSatisfySearchConditionByMethod_ReturnsEmpty() {
        Route route1 = new Route().setMethod(METHOD).setPath(PATH).setAlt(ALT1);
        when(configRepository.findAllRoutes()).thenReturn(List.of(route1));

        RouteService service = service();

        assertTrue(service.getRandomAltFor(METHOD_OTHER, PATH).isEmpty());
    }

    @Test
    public void getRandomAltFor_NoRoutesSatisfySearchConditionByPath_ReturnsEmpty() {
        Route route1 = new Route().setMethod(METHOD).setPath(PATH).setAlt(ALT1);
        Route route2 = new Route().setMethod(METHOD).setPath(PATH).setAlt(ALT2);
        when(configRepository.findAllRoutes()).thenReturn(List.of(route1, route2));

        RouteService service = service();

        assertTrue(service.getRandomAltFor(METHOD, PATH_OTHER).isEmpty());
    }

    @Test
    public void getRandomAltFor_AnyNumberOfRoutesSatisfySearchConditionButAreDisabled_ReturnsEmpty() {
        Route route1 = new Route().setMethod(METHOD).setPath(PATH).setAlt(ALT1).setDisabled(true);
        Route route2 = new Route().setMethod(METHOD).setPath(PATH).setAlt(ALT2).setDisabled(true);
        when(configRepository.findAllRoutes()).thenReturn(List.of(route1, route2));

        RouteService service = service();

        assertTrue(service.getRandomAltFor(METHOD, PATH).isEmpty());
    }
}
