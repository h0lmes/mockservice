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

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class RouteServiceImplTest {

    private static final RequestMethod METHOD = RequestMethod.POST;
    private static final String PATH = "/test";
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
    public void getEnabledRoute_RouteDisabled_ReturnsRoute() {
        Route route = new Route().setPath(PATH).setDisabled(true);
        when(configRepository.findRoute(any())).thenReturn(Optional.of(route));

        RouteService service = createRouteService();
        assertFalse(service.getEnabledRoute(route).isPresent());
    }

    @Test
    public void getRandomAltFor_ExistingRoutes_ReturnsAltOfOneOfExistingRoutes() {
        Route route1 = new Route().setMethod(METHOD).setPath(PATH).setAlt(ALT1);
        Route route2 = new Route().setMethod(METHOD).setPath(PATH).setAlt(ALT2);
        when(configRepository.findAllRoutes()).thenReturn(List.of(route1, route2));

        RouteService service = createRouteService();

        Optional<String> alt = service.getRandomAltFor(METHOD, PATH);
        assertTrue(alt.isPresent());
        assertTrue(Set.of(ALT1, ALT2).contains(alt.get()));
    }
}
