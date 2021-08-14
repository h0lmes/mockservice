package com.mockservice.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.mockservice.domain.Route;
import com.mockservice.domain.Scenario;
import com.mockservice.domain.ScenarioAlreadyExistsException;
import com.mockservice.domain.Settings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class ConfigRepositoryImplTest {

    private static final String ROUTE_PATH = "/api/path";
    private static final String OLD_RESPONSE = "old";
    private static final String NEW_RESPONSE = "new";
    private static final String ALIAS = "default";
    private static final String NEW_ALIAS = "new";

    @Mock
    ConfigChangedListener configChangedListener;
    @Mock
    RoutesChangedListener routesChangedListener;
    @Mock
    ScenariosChangedListener scenariosChangedListener;

    @TempDir
    File folder; // must not be private

    private ConfigRepository createConfigRepository() {
        ConfigRepositoryImpl configRepository = new ConfigRepositoryImpl(
                getTempFile("config.yml"),
                getTempFile("backup.yml"),
                getYamlMapper());

        configRepository.registerConfigChangedListener(configChangedListener);
        configRepository.registerRoutesChangedListener(routesChangedListener);
        configRepository.registerScenariosChangedListener(scenariosChangedListener);

        return configRepository;
    }

    private String getTempFile(String fileName) {
        return new File(folder, fileName).getAbsolutePath();
    }

    private ObjectMapper getYamlMapper() {
        YAMLFactory factory = new YAMLFactory();
        ObjectMapper yamlMapper = new ObjectMapper(factory);
        yamlMapper.findAndRegisterModules();
        return yamlMapper;
    }

    //----------------------------------------------------------------------
    //
    //   Config
    //
    //----------------------------------------------------------------------

    @Test
    public void getConfigData_DefaultConfig_ReturnsNotEmptyConfigData() throws JsonProcessingException {
        ConfigRepository configRepository = createConfigRepository();
        assertNotEquals("", configRepository.getConfigData());
    }

    @Test
    public void writeConfigData_DefaultConfig_ListenersCalled() throws IOException {
        ConfigRepository configRepository = createConfigRepository();

        String configData = configRepository.getConfigData();
        configRepository.writeConfigData(configData);

        verify(configChangedListener, times(1)).onBeforeConfigChanged();
        verify(configChangedListener, times(1)).onAfterConfigChanged();
    }

    @Test
    public void backupAndRestore_NonDefaultConfig_ConfigRestored() throws IOException {
        ConfigRepository configRepository = createConfigRepository();
        Route route = new Route().setPath(ROUTE_PATH);
        configRepository.putRoute(route);

        String configData = configRepository.getConfigData();

        configRepository.backup();
        configRepository.deleteRoutes(List.of(route));
        configRepository.restore();

        assertEquals(configData, configRepository.getConfigData());
    }

    @Test
    public void restore_ListenersCalled() throws IOException {
        ConfigRepository configRepository = createConfigRepository();

        configRepository.backup();
        configRepository.restore();

        verify(configChangedListener, times(1)).onBeforeConfigChanged();
        verify(configChangedListener, times(1)).onAfterConfigChanged();
    }

    //----------------------------------------------------------------------
    //
    //   Settings
    //
    //----------------------------------------------------------------------

    @Test
    public void getSettings_DefaultConfig_ReturnsNonNullSettings() {
        ConfigRepository configRepository = createConfigRepository();
        assertNotNull(configRepository.getSettings());
    }

    @Test
    public void setSettings_NonDefaultSettings_ReturnsEqualSettings() throws IOException {
        ConfigRepository configRepository = createConfigRepository();

        Settings settings = new Settings();
        settings.setQuantum(!settings.getQuantum());
        configRepository.setSettings(settings);

        Settings expected = new Settings();
        expected.setQuantum(!expected.getQuantum());
        assertEquals(expected, configRepository.getSettings());
    }

    //----------------------------------------------------------------------
    //
    //   Routes
    //
    //----------------------------------------------------------------------

    @Test
    public void putRoute_NonExisting_RouteAdded() throws IOException {
        ConfigRepository configRepository = createConfigRepository();

        Route route = new Route().setPath(ROUTE_PATH);
        configRepository.putRoute(route);

        List<Route> allRoutes = configRepository.findAllRoutes();
        assertEquals(1, allRoutes.size());
        assertEquals(route, allRoutes.get(0));
    }

    @Test
    public void putRoute_NonExisting_ListenerCalled() throws IOException {
        ConfigRepository configRepository = createConfigRepository();

        Route route = new Route().setPath(ROUTE_PATH);
        configRepository.putRoute(route);

        verify(routesChangedListener, times(1)).onRouteCreated(any());
    }

    @Test
    public void putRoute_Existing_RouteUpdated() throws IOException {
        ConfigRepository configRepository = createConfigRepository();

        Route route1 = new Route().setPath(ROUTE_PATH).setResponse(OLD_RESPONSE);
        Route route2 = new Route().setPath(ROUTE_PATH).setResponse(NEW_RESPONSE);
        configRepository.putRoute(route1);
        configRepository.putRoute(route2);

        Route routeSearch = new Route().setPath(ROUTE_PATH);
        Optional<Route> routeOptional = configRepository.findRoute(routeSearch);
        assertTrue(routeOptional.isPresent());
        assertEquals(NEW_RESPONSE, routeOptional.get().getResponse());
    }

    @Test
    public void putRoute_Existing_ListenersCalled() throws IOException {
        ConfigRepository configRepository = createConfigRepository();

        Route route1 = new Route().setPath(ROUTE_PATH).setResponse(OLD_RESPONSE);
        Route route2 = new Route().setPath(ROUTE_PATH).setResponse(NEW_RESPONSE);

        configRepository.putRoute(route1);
        verify(routesChangedListener, times(1)).onRouteCreated(any());
        verify(routesChangedListener, never()).onRouteDeleted(any());

        configRepository.putRoute(route2);
        verify(routesChangedListener, times(2)).onRouteCreated(any());
        verify(routesChangedListener, times(1)).onRouteDeleted(any());
    }

    @Test
    public void putRoutes_NonExisting_RoutesAdded() throws IOException {
        ConfigRepository configRepository = createConfigRepository();

        Route route1 = new Route().setPath(ROUTE_PATH);
        Route route2 = new Route().setPath(ROUTE_PATH).setMethod(RequestMethod.POST);
        configRepository.putRoutes(List.of(route1, route2), true);

        List<Route> allRoutes = configRepository.findAllRoutes();
        assertEquals(2, allRoutes.size());
    }

    @Test
    public void putRoutes_NonExisting_ListenerCalled() throws IOException {
        ConfigRepository configRepository = createConfigRepository();

        Route route1 = new Route().setPath(ROUTE_PATH);
        Route route2 = new Route().setPath(ROUTE_PATH).setMethod(RequestMethod.POST);
        configRepository.putRoutes(List.of(route1, route2), true);

        verify(routesChangedListener, times(2)).onRouteCreated(any());
    }

    @Test
    public void putRoutes_OneOfTwoExists_OverwriteFalse_OneRouteAddedAndOtherOneNotModified() throws IOException {
        ConfigRepository configRepository = createConfigRepository();

        Route route1 = new Route().setPath(ROUTE_PATH);
        Route route2 = new Route().setPath(ROUTE_PATH).setMethod(RequestMethod.POST).setResponse(OLD_RESPONSE);
        configRepository.putRoutes(List.of(route1, route2), false);

        assertEquals(2, configRepository.findAllRoutes().size());

        Route route3 = new Route().setPath(ROUTE_PATH).setMethod(RequestMethod.DELETE);
        Route route4 = new Route(route2).setResponse(NEW_RESPONSE);
        configRepository.putRoutes(List.of(route3, route4), false);

        assertEquals(3, configRepository.findAllRoutes().size());
        assertEquals(OLD_RESPONSE, configRepository.findRoute(route4).get().getResponse());
    }

    @Test
    public void putRoutes_OneOfTwoExists_OverwriteTrue_OneRouteAddedAndOtherOneModified() throws IOException {
        ConfigRepository configRepository = createConfigRepository();

        Route route1 = new Route().setPath(ROUTE_PATH);
        Route route2 = new Route().setPath(ROUTE_PATH).setMethod(RequestMethod.POST).setResponse(OLD_RESPONSE);
        configRepository.putRoutes(List.of(route1, route2), false);

        assertEquals(2, configRepository.findAllRoutes().size());

        Route route3 = new Route().setPath(ROUTE_PATH).setMethod(RequestMethod.DELETE);
        Route route4 = new Route(route2).setResponse(NEW_RESPONSE);
        configRepository.putRoutes(List.of(route3, route4), true);

        assertEquals(3, configRepository.findAllRoutes().size());
        assertEquals(NEW_RESPONSE, configRepository.findRoute(route4).get().getResponse());
    }

    @Test
    public void deleteRoutes_AddThenDeleteSameRoute_NoRoutesFound() throws IOException {
        ConfigRepository configRepository = createConfigRepository();

        Route route1 = new Route().setPath(ROUTE_PATH);
        configRepository.putRoute(route1);

        assertEquals(1, configRepository.findAllRoutes().size());

        Route route2 = new Route().setPath(ROUTE_PATH);
        configRepository.deleteRoutes(List.of(route2));

        assertEquals(0, configRepository.findAllRoutes().size());
    }

    @Test
    public void deleteRoutes_AddThenDeleteSameRoute_ListenerCalled() throws IOException {
        ConfigRepository configRepository = createConfigRepository();

        Route route1 = new Route().setPath(ROUTE_PATH);
        configRepository.putRoute(route1);

        assertEquals(1, configRepository.findAllRoutes().size());

        Route route2 = new Route(route1);
        configRepository.deleteRoutes(List.of(route2));

        verify(routesChangedListener, times(1)).onRouteDeleted(any());
    }

    @Test
    public void deleteRoutes_AddThenDeleteDifferentRoute_OneRouteFound() throws IOException {
        ConfigRepository configRepository = createConfigRepository();

        Route route1 = new Route().setPath(ROUTE_PATH).setMethod(RequestMethod.GET);
        configRepository.putRoute(route1);

        assertEquals(1, configRepository.findAllRoutes().size());

        Route route2 = new Route().setPath(ROUTE_PATH).setMethod(RequestMethod.DELETE);
        configRepository.deleteRoutes(List.of(route2));

        assertEquals(1, configRepository.findAllRoutes().size());
    }

    //----------------------------------------------------------------------
    //
    //   Scenarios
    //
    //----------------------------------------------------------------------

    @Test
    public void putScenario_Existing_Throws() throws IOException {
        ConfigRepository configRepository = createConfigRepository();

        Scenario dummy = new Scenario();
        Scenario scenario1 = new Scenario().setAlias(ALIAS);
        configRepository.putScenario(dummy, scenario1);

        assertEquals(1, configRepository.findAllScenarios().size());

        Scenario scenario2 = new Scenario().setAlias(NEW_ALIAS);

        assertThrows(ScenarioAlreadyExistsException.class,
                () -> configRepository.putScenario(scenario2, scenario1));
    }

    @Test
    public void putScenario_ReplaceExisting_ScenarioUpdated() throws IOException {
        ConfigRepository configRepository = createConfigRepository();

        Scenario dummy = new Scenario();
        Scenario scenario1 = new Scenario().setAlias(ALIAS);
        configRepository.putScenario(dummy, scenario1);

        assertEquals(1, configRepository.findAllScenarios().size());

        Scenario scenario2 = new Scenario().setAlias(NEW_ALIAS);
        configRepository.putScenario(scenario1, scenario2);

        assertEquals(1, configRepository.findAllScenarios().size());
        assertEquals(NEW_ALIAS, configRepository.findAllScenarios().get(0).getAlias());
    }

    @Test
    public void putScenario_ReplaceExisting_ListenerCalled() throws IOException {
        ConfigRepository configRepository = createConfigRepository();

        Scenario dummy = new Scenario();
        Scenario scenario1 = new Scenario().setAlias(ALIAS);
        configRepository.putScenario(dummy, scenario1);

        Scenario scenario2 = new Scenario().setAlias(NEW_ALIAS);
        configRepository.putScenario(scenario1, scenario2);

        verify(scenariosChangedListener, times(2)).onScenarioUpdated(any(), any());
    }

    @Test
    public void deleteScenario_AddThenDeleteSameScenario_NoScenariosFound() throws IOException {
        ConfigRepository configRepository = createConfigRepository();

        Scenario dummy = new Scenario();
        Scenario scenario1 = new Scenario().setAlias(ALIAS);
        configRepository.putScenario(dummy, scenario1);

        assertEquals(1, configRepository.findAllScenarios().size());

        Scenario scenario2 = new Scenario().setAlias(ALIAS);
        configRepository.deleteScenario(scenario2);

        assertEquals(0, configRepository.findAllScenarios().size());
    }

    @Test
    public void deleteScenario_AddThenDeleteSameScenario_ListenerCalled() throws IOException {
        ConfigRepository configRepository = createConfigRepository();

        Scenario dummy = new Scenario();
        Scenario scenario1 = new Scenario().setAlias(ALIAS);
        configRepository.putScenario(dummy, scenario1);

        assertEquals(1, configRepository.findAllScenarios().size());

        Scenario scenario2 = new Scenario(scenario1);
        configRepository.deleteScenario(scenario2);

        verify(scenariosChangedListener, times(1)).onScenarioDeleted(any());
    }

    @Test
    public void deleteScenario_AddThenDeleteDifferentScenario_OneScenarioFound() throws IOException {
        ConfigRepository configRepository = createConfigRepository();

        Scenario dummy = new Scenario();
        Scenario scenario1 = new Scenario().setAlias(ALIAS);
        configRepository.putScenario(dummy, scenario1);

        assertEquals(1, configRepository.findAllScenarios().size());

        Scenario scenario2 = new Scenario().setAlias(NEW_ALIAS);
        configRepository.deleteScenario(scenario2);

        assertEquals(1, configRepository.findAllScenarios().size());
    }
}
