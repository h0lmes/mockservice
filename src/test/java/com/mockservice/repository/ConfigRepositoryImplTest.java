package com.mockservice.repository;

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
import com.mockservice.domain.Route;
import com.mockservice.domain.Scenario;
import com.mockservice.domain.ScenarioAlreadyExistsException;
import com.mockservice.domain.Settings;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
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
    private static final String OLD_RESPONSE = "old";
    private static final String NEW_RESPONSE = "new";
    private static final String ALIAS = "default";
    private static final String NEW_ALIAS = "new";

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
    public void backupAndRestore_NonDefaultConfig_ConfigRestored() throws IOException {
        ConfigRepository configRepository = repository();
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
        ConfigRepository configRepository = repository();

        configRepository.backup();
        configRepository.restore();

        verify(configObserver, times(1)).onBeforeConfigChanged();
        verify(configObserver, times(1)).onAfterConfigChanged();
    }

    //----------------------------------------------------------------------
    //
    //   Settings
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
        ConfigRepository configRepository = repository();

        Route route = new Route().setPath(ROUTE_PATH);
        configRepository.putRoute(route);

        List<Route> allRoutes = configRepository.findAllRoutes();
        assertEquals(1, allRoutes.size());
        assertEquals(route, allRoutes.get(0));
    }

    @Test
    public void putRoute_NonExisting_ListenerCalled() throws IOException {
        ConfigRepository configRepository = repository();

        Route route = new Route().setPath(ROUTE_PATH);
        configRepository.putRoute(route);

        verify(routeObserver, times(1)).onRouteCreated(any());
    }

    @Test
    public void putRoute_Existing_RouteUpdated() throws IOException {
        ConfigRepository configRepository = repository();

        Route route1 = new Route().setId("1").setPath(ROUTE_PATH).setResponse(OLD_RESPONSE);
        Route route2 = new Route().setId("1").setPath(ROUTE_PATH).setResponse(NEW_RESPONSE);
        configRepository.putRoute(route1);
        configRepository.putRoute(route2);

        Route routeSearch = new Route().setPath(ROUTE_PATH);
        Optional<Route> routeOptional = configRepository.findRoute(routeSearch);
        assertTrue(routeOptional.isPresent());
        assertEquals(NEW_RESPONSE, routeOptional.get().getResponse());
    }

    @Test
    public void putRoute_Existing_ListenersCalled() throws IOException {
        ConfigRepository configRepository = repository();

        Route route1 = new Route().setId("1").setPath(ROUTE_PATH).setResponse(OLD_RESPONSE);
        Route route2 = new Route().setId("1").setPath(ROUTE_PATH).setResponse(NEW_RESPONSE);

        configRepository.putRoute(route1);
        verify(routeObserver, times(1)).onRouteCreated(any());
        verify(routeObserver, never()).onRouteDeleted(any());

        configRepository.putRoute(route2);
        verify(routeObserver, times(2)).onRouteCreated(any());
        verify(routeObserver, times(1)).onRouteDeleted(any());
    }

    @Test
    public void putRoutes_NonExisting_RoutesAdded() throws IOException {
        ConfigRepository configRepository = repository();

        Route route1 = new Route().setPath(ROUTE_PATH);
        Route route2 = new Route().setPath(ROUTE_PATH).setMethod(RequestMethod.POST);
        configRepository.putRoutes(List.of(route1, route2), true);

        List<Route> allRoutes = configRepository.findAllRoutes();
        assertEquals(2, allRoutes.size());
    }

    @Test
    public void putRoutes_NonExisting_ListenerCalled() throws IOException {
        ConfigRepository configRepository = repository();

        Route route1 = new Route().setPath(ROUTE_PATH);
        Route route2 = new Route().setPath(ROUTE_PATH).setMethod(RequestMethod.POST);
        configRepository.putRoutes(List.of(route1, route2), true);

        verify(routeObserver, times(2)).onRouteCreated(any());
    }

    @Test
    public void putRoutes_OneOfTwoExists_OverwriteFalse_OneRouteAddedAndOtherNotModified() throws IOException {
        ConfigRepository repository = repository();

        Route route1 = new Route().setPath(ROUTE_PATH);
        Route route2 = new Route().setPath(ROUTE_PATH).setMethod(RequestMethod.POST).setResponse(OLD_RESPONSE);
        repository.putRoutes(List.of(route1, route2), false);

        Route route3 = new Route().setPath(ROUTE_PATH).setMethod(RequestMethod.DELETE);
        Route route4 = new Route().setPath(ROUTE_PATH).setMethod(RequestMethod.PATCH).setResponse(NEW_RESPONSE);
        repository.findRoute(route2).ifPresent(r -> route4.setId(r.getId()));

        repository.putRoutes(List.of(route3, route4), false);

        assertEquals(3, repository.findAllRoutes().size());
        assertEquals(NEW_RESPONSE, repository.findRoute(route2).get().getResponse());
    }

    @Test
    public void putRoutes_OneOfTwoExists_OverwriteTrue_OneRouteAddedAndOtherModified() throws IOException {
        ConfigRepository configRepository = repository();

        Route route1 = new Route().setPath(ROUTE_PATH);
        Route route2 = new Route().setPath(ROUTE_PATH).setMethod(RequestMethod.POST).setResponse(OLD_RESPONSE);
        configRepository.putRoutes(List.of(route1, route2), true);

        Route routeFound = configRepository.findRoute(route2).orElse(null);
        assertNotNull(routeFound);

        Route route3 = new Route().setPath(ROUTE_PATH).setMethod(RequestMethod.DELETE);
        Route route4 = new Route().setPath(ROUTE_PATH).setMethod(RequestMethod.PATCH).setResponse(NEW_RESPONSE);
        route4.setId(routeFound.getId());
        configRepository.putRoutes(List.of(route3, route4), true);

        assertEquals(3, configRepository.findAllRoutes().size());
        Route route4Found = configRepository.findRoute(route4).orElse(null);
        assertNotNull(route4Found);
        assertEquals(NEW_RESPONSE, route4Found.getResponse());
    }

    @DisplayName("4.id = 2.id and 4.equals(1) => 2 is being rewritten by 4, thus 2.equals(1)")
    @Test
    public void putRoutes_OneOfTwoExistsByIdAndKeyFields_OverwriteTrue_Throws() throws IOException {
        ConfigRepository repository = repository();

        Route route1 = new Route().setPath(ROUTE_PATH).setMethod(RequestMethod.GET);
        Route route2 = new Route().setPath(ROUTE_PATH).setMethod(RequestMethod.POST).setResponse(OLD_RESPONSE);
        repository.putRoutes(List.of(route1, route2), true);

        Route routeFound = repository.findRoute(route2).orElse(null);
        assertNotNull(routeFound);

        Route route3 = new Route().setPath(ROUTE_PATH).setMethod(RequestMethod.DELETE);
        Route route4 = new Route().setPath(ROUTE_PATH).setMethod(RequestMethod.GET).setResponse(NEW_RESPONSE);
        route4.setId(routeFound.getId());

        repository.putRoutes(List.of(route3, route4), true);

        List<Route> allRoutes = repository.findAllRoutes();
        assertEquals(3, allRoutes.size());

        Stream<Route> equalRoutes = allRoutes.stream()
                .filter(r -> r.getMethod().equals(route4.getMethod())
                        && r.getPath().equals(route4.getPath())
                        && r.getAlt().equals(route4.getAlt()));
        assertEquals(2, equalRoutes.count());
    }

    @Test
    public void deleteRoutes_AddThenDeleteSameRoute_NoRoutesFound() throws IOException {
        ConfigRepository configRepository = repository();

        Route route1 = new Route().setPath(ROUTE_PATH);
        configRepository.putRoute(route1);

        assertEquals(1, configRepository.findAllRoutes().size());

        Route route2 = new Route().setPath(ROUTE_PATH);
        configRepository.deleteRoutes(List.of(route2));

        assertEquals(0, configRepository.findAllRoutes().size());
    }

    @Test
    public void deleteRoutes_AddThenDeleteSameRoute_ListenerCalled() throws IOException {
        ConfigRepository configRepository = repository();

        Route route1 = new Route().setPath(ROUTE_PATH);
        configRepository.putRoute(route1);

        assertEquals(1, configRepository.findAllRoutes().size());

        Route route2 = new Route(route1);
        configRepository.deleteRoutes(List.of(route2));

        verify(routeObserver, times(1)).onRouteDeleted(any());
    }

    @Test
    public void deleteRoutes_AddThenDeleteDifferentRoute_OneRouteFound() throws IOException {
        ConfigRepository configRepository = repository();

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
    public void putScenario_NotExisting_Success() throws IOException {
        ConfigRepository configRepository = repository();

        Scenario scenario = new Scenario().setAlias(ALIAS);
        configRepository.putScenario(scenario);

        assertEquals(1, configRepository.findAllScenarios().size());
    }

    @Test
    public void putScenario_Existing_Throws() throws IOException {
        ConfigRepository configRepository = repository();

        Scenario scenario = new Scenario().setAlias(ALIAS);
        configRepository.putScenario(scenario);

        Scenario scenario2 = new Scenario().setAlias(ALIAS);

        assertThrows(ScenarioAlreadyExistsException.class,
                () -> configRepository.putScenario(scenario2));
    }

    @Test
    public void putScenario_ReplaceExistingById_ScenarioUpdated() throws IOException {
        ConfigRepository configRepository = repository();

        Scenario scenario1 = new Scenario().setAlias(ALIAS);
        configRepository.putScenario(scenario1);

        scenario1.setAlias(NEW_ALIAS);
        configRepository.putScenario(scenario1);

        assertEquals(1, configRepository.findAllScenarios().size());
        assertEquals(NEW_ALIAS, configRepository.findAllScenarios().get(0).getAlias());
    }

    @Test
    public void deleteScenario_AddThenDeleteSameScenario_NoScenariosFound() throws IOException {
        ConfigRepository configRepository = repository();

        Scenario scenario1 = new Scenario().setAlias(ALIAS);
        configRepository.putScenario(scenario1);

        Scenario scenario2 = new Scenario().setAlias(ALIAS);
        configRepository.deleteScenario(scenario2);

        assertEquals(0, configRepository.findAllScenarios().size());
    }

    @Test
    public void deleteScenario_AddThenDeleteSameScenario_ListenerCalled() throws IOException {
        ConfigRepository configRepository = repository();

        Scenario scenario1 = new Scenario().setAlias(ALIAS);
        configRepository.putScenario(scenario1);

        Scenario scenario2 = new Scenario(scenario1);
        configRepository.deleteScenario(scenario2);

        verify(scenarioObserver, times(1)).onScenarioDeleted(any());
    }

    @Test
    public void deleteScenario_AddThenDeleteDifferentScenario_OneScenarioFound() throws IOException {
        ConfigRepository configRepository = repository();

        Scenario scenario1 = new Scenario().setAlias(ALIAS);
        configRepository.putScenario(scenario1);

        Scenario scenario2 = new Scenario().setAlias(NEW_ALIAS);
        configRepository.deleteScenario(scenario2);

        assertEquals(1, configRepository.findAllScenarios().size());
    }
}
