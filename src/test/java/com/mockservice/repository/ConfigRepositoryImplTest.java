package com.mockservice.repository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.mockservice.domain.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.annotation.RequestMethod;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class ConfigRepositoryImplTest {

    private static final String ROUTE_PATH = "/api/path";
    private static final String STR1 = "AAA";
    private static final String STR2 = "BBB";

    @Mock
    private ConfigObserver configObserver;
    @Mock
    private RouteObserver routeObserver;
    @Mock
    private ScenarioObserver scenarioObserver;

    @TempDir
    File folder; // must not be private

    private ConfigRepository repository() {
        ConfigRepositoryImpl configRepository = new ConfigRepositoryImpl(
                getTempFile("config.yml"),
                getTempFile("backup.yml"),
                getYamlMapper());

        configRepository.setConfigObservers(List.of(configObserver));
        configRepository.setRouteObservers(List.of(routeObserver));
        configRepository.setScenarioObservers(List.of(scenarioObserver));

        return configRepository;
    }

    private ConfigRepository repositoryWithNoObservers() {
        return new ConfigRepositoryImpl(
                getTempFile("config.yml"),
                getTempFile("backup.yml"),
                getYamlMapper());
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
    //
    //
    //   Config
    //
    //
    //
    //----------------------------------------------------------------------

    @Test
    public void getConfigData_DefaultConfig_ReturnsNotEmptyConfigData() throws JsonProcessingException {
        ConfigRepository configRepository = repository();
        assertNotEquals("", configRepository.getConfigData());
    }

    @Test
    public void writeConfigData_DefaultConfig_ListenersCalled() throws IOException {
        ConfigRepository configRepository = repository();

        String configData = configRepository.getConfigData();
        configRepository.writeConfigData(configData);

        verify(configObserver, times(1)).onBeforeConfigChanged();
        verify(configObserver, times(1)).onAfterConfigChanged();
    }

    @Test
    public void writeConfigData_NoObservers_DoesNotThrow() throws IOException {
        ConfigRepository configRepository = repositoryWithNoObservers();
        String configData = configRepository.getConfigData();

        assertDoesNotThrow(() -> configRepository.writeConfigData(configData));
    }

    @Test
    public void backupAndRestore_NonDefaultConfig_ConfigRestored() throws IOException {
        ConfigRepository configRepository = repository();
        Route route = new Route().setPath(ROUTE_PATH);
        configRepository.putRoute(null, route);

        String configData = configRepository.getConfigData();

        configRepository.backup();
        configRepository.deleteRoutes(List.of(route));
        configRepository.restore();

        assertEquals(configData, configRepository.getConfigData());
    }

    @Test
    public void restore_ListenersCalled() throws IOException {
        ConfigRepository configRepository = repository();

        configRepository.backup();
        configRepository.restore();

        verify(configObserver, times(1)).onBeforeConfigChanged();
        verify(configObserver, times(1)).onAfterConfigChanged();
    }

    @Test
    public void writeConfigData_WritePreviouslyReadData_ConfigRestored() throws IOException {
        ConfigRepository configRepository = repository();
        Route route = new Route().setPath(ROUTE_PATH);
        configRepository.putRoute(null, route);
        String configData = configRepository.getConfigData();

        configRepository = repository();
        configRepository.writeConfigData(configData);

        assertTrue(configRepository.findRoute(route).isPresent());
    }

    @Test
    public void writeConfigData_EmptyString_Throws() {
        ConfigRepository configRepository = repository();

        assertThrows(IOException.class, () -> configRepository.writeConfigData(""));
    }

    //----------------------------------------------------------------------
    //
    //
    //
    //   Settings
    //
    //
    //
    //----------------------------------------------------------------------

    @Test
    public void getSettings_DefaultConfig_ReturnsNonNullSettings() {
        ConfigRepository configRepository = repository();
        assertNotNull(configRepository.getSettings());
    }

    @Test
    public void setSettings_NonDefaultSettings_ReturnsEqualSettings() throws IOException {
        ConfigRepository configRepository = repository();

        Settings settings = new Settings();
        boolean expected = !settings.getQuantum();
        settings.setQuantum(expected);
        configRepository.setSettings(settings);

        assertEquals(expected, configRepository.getSettings().getQuantum());
    }

    //----------------------------------------------------------------------
    //
    //
    //
    //   Routes
    //
    //
    //
    //----------------------------------------------------------------------

    @Test
    public void putRoute_NonExisting_RouteAdded() throws IOException {
        ConfigRepository configRepository = repository();
        Route route = new Route().setPath(ROUTE_PATH);
        configRepository.putRoute(null, route);

        List<Route> allRoutes = configRepository.findAllRoutes();
        assertEquals(1, allRoutes.size());
        assertEquals(route, allRoutes.get(0));
    }

    @Test
    public void putRoute_NonExisting_ListenerCalled() throws IOException {
        ConfigRepository configRepository = repository();
        Route route = new Route().setPath(ROUTE_PATH);
        configRepository.putRoute(null, route);

        verify(routeObserver, times(1)).onRouteCreated(any());
    }

    @DisplayName("Update existing route [1] with contents of [2], updates")
    @Test
    public void putRoute_UpdateExisting_RouteUpdated() throws IOException {
        ConfigRepository configRepository = repository();
        Route route1 = new Route().setMethod(RequestMethod.GET).setPath(ROUTE_PATH).setResponse(STR1);
        Route route2 = new Route().setMethod(RequestMethod.GET).setPath(ROUTE_PATH).setResponse(STR2);
        configRepository.putRoute(null, route1);
        configRepository.putRoute(route1, route2);

        Route routeSearch = new Route().setPath(ROUTE_PATH);
        Optional<Route> routeOptional = configRepository.findRoute(routeSearch);
        assertTrue(routeOptional.isPresent());
        assertEquals(STR2, routeOptional.get().getResponse());
    }

    @DisplayName("Update existing route [1] with contents of [2], calls listener")
    @Test
    public void putRoute_UpdateExisting_ListenersCalled() throws IOException {
        ConfigRepository configRepository = repository();

        Route route1 = new Route().setMethod(RequestMethod.GET).setPath(ROUTE_PATH).setResponse(STR1);
        Route route2 = new Route().setMethod(RequestMethod.GET).setPath(ROUTE_PATH).setResponse(STR2);

        configRepository.putRoute(null, route1);
        verify(routeObserver, times(1)).onRouteCreated(any());
        verify(routeObserver, never()).onRouteDeleted(any());

        configRepository.putRoute(route1, route2);
        verify(routeObserver, times(2)).onRouteCreated(any());
        verify(routeObserver, times(1)).onRouteDeleted(any());
    }

    @Test
    public void putRoute_WithNoObservers_DoesNotThrow() {
        ConfigRepository configRepository = repositoryWithNoObservers();

        Route route1 = new Route().setMethod(RequestMethod.GET).setPath(ROUTE_PATH).setResponse(STR1);
        Route route2 = new Route().setMethod(RequestMethod.GET).setPath(ROUTE_PATH).setResponse(STR2);

        assertDoesNotThrow(() -> configRepository.putRoute(null, route1));
        assertDoesNotThrow(() -> configRepository.putRoute(route1, route2));
    }

    @DisplayName("Try to create new route [2] that equals already existing route [1] -> throws (do not allow duplicates)")
    @Test
    public void putRoute_NewExistsByEquals_Throws() throws IOException {
        ConfigRepository configRepository = repository();
        Route route1 = new Route().setMethod(RequestMethod.GET).setPath(ROUTE_PATH).setResponse(STR1);
        Route route2 = new Route().setMethod(RequestMethod.GET).setPath(ROUTE_PATH).setResponse(STR2);
        configRepository.putRoute(null, route1);
        assertThrows(RouteAlreadyExistsException.class, () -> configRepository.putRoute(null, route2));
    }

    @DisplayName("Update existing route [1] with contents of [3] which equals other existing route [2] -> throws (do not allow duplicates)")
    @Test
    public void putRoute_ReferenceExistsAndNewExistsAndReferenceNotEqualsNew_Throws() throws IOException {
        ConfigRepository configRepository = repository();
        Route route1 = new Route().setMethod(RequestMethod.GET).setPath(ROUTE_PATH).setResponse(STR1);
        Route route2 = new Route().setMethod(RequestMethod.POST).setPath(ROUTE_PATH).setResponse(STR1);
        Route route3 = new Route().setMethod(RequestMethod.POST).setPath(ROUTE_PATH).setResponse(STR2);
        configRepository.putRoute(null, route1);
        configRepository.putRoute(null, route2);
        assertThrows(RouteAlreadyExistsException.class, () -> configRepository.putRoute(route1, route3));
    }

    @Test
    public void putRoutes_NonExisting_RoutesAdded() throws IOException {
        ConfigRepository configRepository = repository();

        Route route1 = new Route().setPath(ROUTE_PATH).setMethod(RequestMethod.GET);
        Route route2 = new Route().setPath(ROUTE_PATH).setMethod(RequestMethod.POST);
        configRepository.putRoutes(List.of(route1, route2), true);

        List<Route> allRoutes = configRepository.findAllRoutes();
        assertEquals(2, allRoutes.size());
    }

    @Test
    public void putRoutes_NonExisting_ListenerCalled() throws IOException {
        ConfigRepository configRepository = repository();

        Route route1 = new Route().setPath(ROUTE_PATH).setMethod(RequestMethod.GET);
        Route route2 = new Route().setPath(ROUTE_PATH).setMethod(RequestMethod.POST);
        configRepository.putRoutes(List.of(route1, route2), true);

        verify(routeObserver, times(2)).onRouteCreated(any());
    }

    @Test
    public void putRoutes_OneOfTwoExists_OverwriteTrue_OneRouteAddedOtherRouteModified() throws IOException {
        ConfigRepository configRepository = repository();

        Route route1 = new Route().setPath(ROUTE_PATH).setMethod(RequestMethod.GET);
        Route route2 = new Route().setPath(ROUTE_PATH).setMethod(RequestMethod.POST).setResponse(STR1);
        configRepository.putRoutes(List.of(route1, route2), true);

        Route routeFound = configRepository.findRoute(route2).orElse(null);
        assertNotNull(routeFound);

        Route route3 = new Route().setPath(ROUTE_PATH).setMethod(RequestMethod.DELETE);
        Route route4 = new Route().setPath(ROUTE_PATH).setMethod(RequestMethod.POST).setResponse(STR2);
        configRepository.putRoutes(List.of(route3, route4), true);

        assertEquals(3, configRepository.findAllRoutes().size());
        Route route4Found = configRepository.findRoute(route4).orElse(null);
        assertNotNull(route4Found);
        assertEquals(STR2, route4Found.getResponse());
    }

    @Test
    public void putRoutes_RouteExists_OverwriteFalse_RouteNotModified_DoesNotThrow() throws IOException {
        ConfigRepository configRepository = repository();
        Route route2 = new Route().setPath(ROUTE_PATH).setMethod(RequestMethod.POST).setResponse(STR1);
        configRepository.putRoutes(List.of(route2), true);
        Route route4 = new Route().setPath(ROUTE_PATH).setMethod(RequestMethod.POST).setResponse(STR2);

        assertDoesNotThrow(() -> configRepository.putRoutes(List.of(route4), false));

        Route route4Found = configRepository.findRoute(route4).orElse(null);

        assertNotNull(route4Found);
        assertEquals(STR1, route4Found.getResponse());
    }

    @Test
    public void deleteRoutes_AddThenDeleteSameRoute_NoRoutesFound() throws IOException {
        ConfigRepository configRepository = repository();

        Route route1 = new Route().setPath(ROUTE_PATH);
        configRepository.putRoute(null, route1);

        assertEquals(1, configRepository.findAllRoutes().size());

        Route route2 = new Route().setPath(ROUTE_PATH);
        configRepository.deleteRoutes(List.of(route2));

        assertEquals(0, configRepository.findAllRoutes().size());
    }

    @Test
    public void deleteRoutes_AddThenDeleteSameRoute_ListenerCalled() throws IOException {
        ConfigRepository configRepository = repository();

        Route route1 = new Route().setPath(ROUTE_PATH);
        configRepository.putRoute(null, route1);

        assertEquals(1, configRepository.findAllRoutes().size());

        Route route2 = new Route(route1);
        configRepository.deleteRoutes(List.of(route2));

        verify(routeObserver, times(1)).onRouteDeleted(any());
    }

    @Test
    public void deleteRoutes_AddThenDeleteDifferentRoute_OneRouteFound() throws IOException {
        ConfigRepository configRepository = repository();

        Route route1 = new Route().setPath(ROUTE_PATH).setMethod(RequestMethod.GET);
        configRepository.putRoute(null, route1);

        assertEquals(1, configRepository.findAllRoutes().size());

        Route route2 = new Route().setPath(ROUTE_PATH).setMethod(RequestMethod.DELETE);
        configRepository.deleteRoutes(List.of(route2));

        assertEquals(1, configRepository.findAllRoutes().size());
    }

    //----------------------------------------------------------------------
    //
    //
    //
    //   Scenarios
    //
    //
    //
    //----------------------------------------------------------------------

    @Test
    public void putScenario_NotExisting_Success() throws IOException {
        ConfigRepository configRepository = repository();

        Scenario scenario = new Scenario().setAlias(STR1);
        configRepository.putScenario(null, scenario);

        assertEquals(1, configRepository.findAllScenarios().size());
    }

    @Test
    public void putScenario_UpdateExisting_UpdatesScenario() throws IOException {
        ConfigRepository configRepository = repository();
        Scenario scenario1 = new Scenario().setAlias(STR1).setData(STR1);
        Scenario scenario2 = new Scenario().setAlias(STR1).setData(STR2);
        configRepository.putScenario(null, scenario1);
        configRepository.putScenario(scenario1, scenario2);
        List<Scenario> scenarios = configRepository.findAllScenarios();

        assertEquals(1, scenarios.size());
        assertEquals(STR2, scenarios.get(0).getData());
    }

    @Test
    public void putScenario_NoOriginalAndTryCreatingCopy_Throws() throws IOException {
        ConfigRepository configRepository = repository();
        Scenario scenario1 = new Scenario().setAlias(STR1).setData(STR1);
        Scenario scenario2 = new Scenario().setAlias(STR1).setData(STR2);
        configRepository.putScenario(null, scenario1);
        assertThrows(ScenarioAlreadyExistsException.class, () -> configRepository.putScenario(null, scenario2));
    }

    @Test
    public void putScenario_OriginalProvidedAndTryCreatingCopy_Throws() throws IOException {
        ConfigRepository configRepository = repository();
        Scenario scenario1 = new Scenario().setAlias(STR1);
        Scenario scenario2 = new Scenario().setAlias(STR2);
        Scenario scenario3 = new Scenario().setAlias(STR2);
        configRepository.putScenario(null, scenario1);
        configRepository.putScenario(null, scenario2);
        assertThrows(ScenarioAlreadyExistsException.class, () -> configRepository.putScenario(scenario1, scenario3));
    }

    @Test
    public void deleteScenario_AddThenDeleteSameScenario_NoScenariosFound() throws IOException {
        ConfigRepository configRepository = repository();

        Scenario scenario1 = new Scenario().setAlias(STR1);
        configRepository.putScenario(null, scenario1);
        configRepository.deleteScenario(scenario1);

        assertEquals(0, configRepository.findAllScenarios().size());
    }

    @Test
    public void deleteScenario_AddThenDeleteSameScenario_ListenerCalled() throws IOException {
        ConfigRepository configRepository = repository();

        Scenario scenario1 = new Scenario().setAlias(STR1);
        configRepository.putScenario(null, scenario1);
        configRepository.deleteScenario(scenario1);

        verify(scenarioObserver, times(1)).onScenarioDeleted(any());
    }

    @Test
    public void deleteScenario_WithNoObservers_DoesNotThrow() throws IOException {
        ConfigRepository configRepository = repositoryWithNoObservers();

        Scenario scenario1 = new Scenario().setAlias(STR1);
        assertDoesNotThrow(() -> configRepository.putScenario(null, scenario1));
        assertDoesNotThrow(() -> configRepository.deleteScenario(scenario1));
    }

    @Test
    public void deleteScenario_AddThenDeleteDifferentScenario_OneScenarioFound() throws IOException {
        ConfigRepository configRepository = repository();

        Scenario scenario1 = new Scenario().setAlias(STR1);
        configRepository.putScenario(null, scenario1);

        Scenario scenario2 = new Scenario().setAlias(STR2);
        configRepository.deleteScenario(scenario2);

        assertEquals(1, configRepository.findAllScenarios().size());
    }
}
