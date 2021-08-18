package com.mockservice.service;

import com.mockservice.domain.Route;
import com.mockservice.repository.ConfigRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class RouteServiceImplTest {

    private static final RequestMethod METHOD = RequestMethod.POST;
    private static final RequestMethod METHOD_OTHER = RequestMethod.PUT;
    private static final String PATH = "/test";
    private static final String PATH_OTHER = "/api/test";
    private static final String ALT1 = "400";
    private static final String ALT2 = "204";

    @Mock
    private ConfigRepository configRepository;

    private RouteService createRouteService() {
        return new RouteServiceImpl(configRepository);
    }

    @Test
    public void getEnabledRoute_RouteEnabled_ReturnsRoute() {
        Route route = new Route().setPath(PATH);
        when(configRepository.findRoute(any())).thenReturn(Optional.of(route));

        RouteService service = createRouteService();

        assertTrue(service.getEnabledRoute(route).isPresent());
    }

    @Test
    public void getEnabledRoute_RouteDisabled_ReturnsEmpty() {
        Route route = new Route().setPath(PATH).setDisabled(true);
        when(configRepository.findRoute(any())).thenReturn(Optional.of(route));

        RouteService service = createRouteService();

        assertTrue(service.getEnabledRoute(route).isEmpty());
    }

    @Test
    public void getRoutesAsList() {
        Route route = new Route().setPath(PATH);
        when(configRepository.findAllRoutes()).thenReturn(List.of(route));

        RouteService service = createRouteService();

        assertTrue(service.getRoutesAsList().contains(route));
        assertThrows(UnsupportedOperationException.class, () -> service.getRoutesAsList().add(route));
    }

    @Test
    public void putRoute() throws IOException {
        Route route = new Route().setPath(PATH);
        when(configRepository.findAllRoutes()).thenReturn(List.of(route));

        RouteService service = createRouteService();
        List<Route> routes = service.putRoute(route);

        verify(configRepository, times(1)).putRoute(route);
        assertTrue(routes.contains(route));
        assertThrows(UnsupportedOperationException.class, () -> routes.add(route));
    }

    @Test
    public void putRoutes() throws IOException {
        Route route = new Route().setPath(PATH);
        when(configRepository.findAllRoutes()).thenReturn(List.of(route));

        RouteService service = createRouteService();
        List<Route> inRoutes = List.of(route);
        List<Route> routes = service.putRoutes(inRoutes, true);

        verify(configRepository, times(1)).putRoutes(inRoutes, true);
        assertTrue(routes.contains(route));
        assertThrows(UnsupportedOperationException.class, () -> routes.add(route));
    }

    @Test
    public void deleteRoutes() throws IOException {
        Route route = new Route().setPath(PATH);
        when(configRepository.findAllRoutes()).thenReturn(List.of(route));

        RouteService service = createRouteService();
        List<Route> inRoutes = List.of(route);
        List<Route> routes = service.deleteRoutes(inRoutes);

        verify(configRepository, times(1)).deleteRoutes(inRoutes);
        assertTrue(routes.contains(route));
        assertThrows(UnsupportedOperationException.class, () -> routes.add(route));
    }

    // --- random alt -----------------------------------------------------

    @Test
    public void getRandomAltFor_MoreThanOneRouteSatisfiesSearchCondition_ReturnsAltOfEither() {
        Route route1 = new Route().setMethod(METHOD).setPath(PATH).setAlt(ALT1);
        Route route2 = new Route().setMethod(METHOD).setPath(PATH).setAlt(ALT2);
        when(configRepository.findAllRoutes()).thenReturn(List.of(route1, route2));

        RouteService service = createRouteService();

        Optional<String> alt = service.getRandomAltFor(METHOD, PATH);
        assertTrue(alt.isPresent());
        assertTrue(Set.of(ALT1, ALT2).contains(alt.get()));
    }

    @Test
    public void getRandomAltFor_OneRouteSatisfiesSearchCondition_ReturnsAltOfTheRoute() {
        Route route1 = new Route().setMethod(METHOD).setPath(PATH).setAlt(ALT1);
        when(configRepository.findAllRoutes()).thenReturn(List.of(route1));

        RouteService service = createRouteService();

        Optional<String> alt = service.getRandomAltFor(METHOD, PATH);
        assertTrue(alt.isPresent());
        assertEquals(ALT1, alt.get());
    }

    @Test
    public void getRandomAltFor_NoRoutesSatisfySearchConditionByMethod_ReturnsEmpty() {
        Route route1 = new Route().setMethod(METHOD).setPath(PATH).setAlt(ALT1);
        when(configRepository.findAllRoutes()).thenReturn(List.of(route1));

        RouteService service = createRouteService();

        assertTrue(service.getRandomAltFor(METHOD_OTHER, PATH).isEmpty());
    }

    @Test
    public void getRandomAltFor_NoRoutesSatisfySearchConditionByPath_ReturnsEmpty() {
        Route route1 = new Route().setMethod(METHOD).setPath(PATH).setAlt(ALT1);
        Route route2 = new Route().setMethod(METHOD).setPath(PATH).setAlt(ALT2);
        when(configRepository.findAllRoutes()).thenReturn(List.of(route1, route2));

        RouteService service = createRouteService();

        assertTrue(service.getRandomAltFor(METHOD, PATH_OTHER).isEmpty());
    }

    @Test
    public void getRandomAltFor_AnyNumberOfRoutesSatisfySearchConditionButAreDisabled_ReturnsEmpty() {
        Route route1 = new Route().setMethod(METHOD).setPath(PATH).setAlt(ALT1).setDisabled(true);
        Route route2 = new Route().setMethod(METHOD).setPath(PATH).setAlt(ALT2).setDisabled(true);
        when(configRepository.findAllRoutes()).thenReturn(List.of(route1, route2));

        RouteService service = createRouteService();

        assertTrue(service.getRandomAltFor(METHOD, PATH).isEmpty());
    }
}
