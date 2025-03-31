package com.mockservice.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.mockservice.domain.OutboundRequest;
import com.mockservice.domain.Route;
import com.mockservice.domain.Scenario;
import com.mockservice.domain.Settings;
import com.mockservice.exception.RouteAlreadyExistsException;
import com.mockservice.exception.ScenarioAlreadyExistsException;
import com.mockservice.model.RouteVariable;
import com.mockservice.template.TemplateEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
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
class ConfigRepositoryImplTest {

    private static final String STR1 = "AAA";
    private static final String STR2 = "BBB";
    private static final String PATH = "/test";
    private static final String RESPONSE_WITH_VARIABLES = "${id} ... ${name:default} ...";

    @Mock
    private TemplateEngine templateEngine;
    @Mock
    private ConfigObserver configObserver;
    @Mock
    private RouteObserver routeObserver;

    @TempDir
    File folder; // must not be private

    private ConfigRepository repository() {
        ConfigRepositoryImpl configRepository = new ConfigRepositoryImpl(
                getTempFile("config.yml"),
                getTempFile("backup.yml"),
                getYamlMapper(),
                templateEngine);

        configRepository.registerConfigObserver(configObserver);
        configRepository.registerRouteObserver(routeObserver);

        return configRepository;
    }

    private ConfigRepository repositoryWithNoObservers() {
        return new ConfigRepositoryImpl(
                getTempFile("config.yml"),
                getTempFile("backup.yml"),
                getYamlMapper(),
                templateEngine);
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
    void getConfigData_DefaultConfig_ReturnsNotEmptyConfigData() throws JsonProcessingException {
        ConfigRepository configRepository = repository();
        assertNotEquals("", configRepository.getConfigData());
    }

    @Test
    void writeConfigData_DefaultConfig_ListenersCalled() throws IOException {
        ConfigRepository configRepository = repository();

        String configData = configRepository.getConfigData();
        configRepository.writeConfigData(configData);

        verify(configObserver, times(1)).onBeforeConfigChanged();
        verify(configObserver, times(1)).onAfterConfigChanged();
    }

    @Test
    void writeConfigData_NoObservers_DoesNotThrow() throws IOException {
        ConfigRepository configRepository = repositoryWithNoObservers();
        String configData = configRepository.getConfigData();

        assertDoesNotThrow(() -> configRepository.writeConfigData(configData));
    }

    @Test
    void backupAndRestore_NonDefaultConfig_ConfigRestored() throws IOException {
        ConfigRepository configRepository = repository();
        Route route = new Route().setPath(PATH);
        configRepository.putRoute(null, route);

        String configData = configRepository.getConfigData();

        configRepository.backup();
        configRepository.deleteRoutes(List.of(route));
        configRepository.restore();

        assertEquals(configData, configRepository.getConfigData());
    }

    @Test
    void restore_ListenersCalled() throws IOException {
        ConfigRepository configRepository = repository();

        configRepository.backup();
        configRepository.restore();

        verify(configObserver, times(1)).onBeforeConfigChanged();
        verify(configObserver, times(1)).onAfterConfigChanged();
    }

    @Test
    void writeConfigData_WritePreviouslyReadData_ConfigRestored() throws IOException {
        ConfigRepository configRepository = repository();
        Route route = new Route().setPath(PATH);
        configRepository.putRoute(null, route);
        String configData = configRepository.getConfigData();

        configRepository = repository();
        configRepository.writeConfigData(configData);

        assertTrue(configRepository.findRoute(route).isPresent());
    }

    @Test
    void writeConfigData_EmptyString_Throws() {
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
    void getSettings_DefaultConfig_ReturnsNonNullSettings() {
        ConfigRepository configRepository = repository();
        assertNotNull(configRepository.getSettings());
    }

    @Test
    void setSettings_NonDefaultSettings_ReturnsEqualSettings() throws IOException {
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
    void getRouteVariables_RouteWithVariables_ReturnsVariables() throws IOException {
        Route route = new Route().setPath(PATH).setResponse(RESPONSE_WITH_VARIABLES);
        ConfigRepository configRepository = repository();
        configRepository.putRoute(null, route);

        List<RouteVariable> routeVariables = configRepository.getRouteVariables(route);

        assertEquals(2, routeVariables.size());

        RouteVariable var0 = routeVariables.get(0);
        RouteVariable var1 = routeVariables.get(1);
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
    void putRoute_NonExisting_RouteAdded() throws IOException {
        ConfigRepository configRepository = repository();
        Route route = new Route().setPath(PATH);
        configRepository.putRoute(null, route);

        List<Route> allRoutes = configRepository.findAllRoutes();
        assertEquals(1, allRoutes.size());
        assertEquals(route, allRoutes.get(0));
    }

    @Test
    void putRoute_NonExisting_ListenerCalled() throws IOException {
        ConfigRepository configRepository = repository();
        Route route = new Route().setPath(PATH);
        configRepository.putRoute(null, route);

        verify(routeObserver, times(1)).onRouteCreated(any());
    }

    @DisplayName("Update existing route [1] with contents of [2], updates")
    @Test
    void putRoute_UpdateExisting_RouteUpdated() throws IOException {
        ConfigRepository configRepository = repository();
        Route route1 = new Route().setMethod(RequestMethod.GET).setPath(PATH).setResponse(STR1);
        Route route2 = new Route().setMethod(RequestMethod.GET).setPath(PATH).setResponse(STR2);
        configRepository.putRoute(null, route1);
        configRepository.putRoute(route1, route2);

        Route routeSearch = new Route().setPath(PATH);
        Optional<Route> routeOptional = configRepository.findRoute(routeSearch);
        assertTrue(routeOptional.isPresent());
        assertEquals(STR2, routeOptional.get().getResponse());
    }

    @DisplayName("Update existing route [1] with contents of [2], calls listener")
    @Test
    void putRoute_UpdateExisting_ListenersCalled() throws IOException {
        ConfigRepository configRepository = repository();

        Route route1 = new Route().setMethod(RequestMethod.GET).setPath(PATH).setResponse(STR1);
        Route route2 = new Route().setMethod(RequestMethod.GET).setPath(PATH).setResponse(STR2);

        configRepository.putRoute(null, route1);
        verify(routeObserver, times(1)).onRouteCreated(any());
        verify(routeObserver, never()).onRouteDeleted(any());

        configRepository.putRoute(route1, route2);
        verify(routeObserver, times(2)).onRouteCreated(any());
        verify(routeObserver, times(1)).onRouteDeleted(any());
    }

    @Test
    void putRoute_WithNoObservers_DoesNotThrow() {
        ConfigRepository configRepository = repositoryWithNoObservers();

        Route route1 = new Route().setMethod(RequestMethod.GET).setPath(PATH).setResponse(STR1);
        Route route2 = new Route().setMethod(RequestMethod.GET).setPath(PATH).setResponse(STR2);

        assertDoesNotThrow(() -> configRepository.putRoute(null, route1));
        assertDoesNotThrow(() -> configRepository.putRoute(route1, route2));
    }

    @DisplayName("Try to create new route [2] that equals already existing route [1] -> throws (do not allow duplicates)")
    @Test
    void putRoute_NewExistsByEquals_Throws() throws IOException {
        ConfigRepository configRepository = repository();
        Route route1 = new Route().setMethod(RequestMethod.GET).setPath(PATH).setResponse(STR1);
        Route route2 = new Route().setMethod(RequestMethod.GET).setPath(PATH).setResponse(STR2);
        configRepository.putRoute(null, route1);
        assertThrows(RouteAlreadyExistsException.class, () -> configRepository.putRoute(null, route2));
    }

    @DisplayName("Update existing route [1] with contents of [3] which equals other existing route [2] -> throws (do not allow duplicates)")
    @Test
    void putRoute_ReferenceExistsAndNewExistsAndReferenceNotEqualsNew_Throws() throws IOException {
        ConfigRepository configRepository = repository();
        Route route1 = new Route().setMethod(RequestMethod.GET).setPath(PATH).setResponse(STR1);
        Route route2 = new Route().setMethod(RequestMethod.POST).setPath(PATH).setResponse(STR1);
        Route route3 = new Route().setMethod(RequestMethod.POST).setPath(PATH).setResponse(STR2);
        configRepository.putRoute(null, route1);
        configRepository.putRoute(null, route2);
        assertThrows(RouteAlreadyExistsException.class, () -> configRepository.putRoute(route1, route3));
    }

    @Test
    void putRoutes_NonExisting_RoutesAdded() throws IOException {
        ConfigRepository configRepository = repository();

        Route route1 = new Route().setPath(PATH).setMethod(RequestMethod.GET);
        Route route2 = new Route().setPath(PATH).setMethod(RequestMethod.POST);
        configRepository.putRoutes(List.of(route1, route2), true);

        List<Route> allRoutes = configRepository.findAllRoutes();
        assertEquals(2, allRoutes.size());
    }

    @Test
    void putRoutes_NonExisting_ListenerCalled() throws IOException {
        ConfigRepository configRepository = repository();

        Route route1 = new Route().setPath(PATH).setMethod(RequestMethod.GET);
        Route route2 = new Route().setPath(PATH).setMethod(RequestMethod.POST);
        configRepository.putRoutes(List.of(route1, route2), true);

        verify(routeObserver, times(2)).onRouteCreated(any());
    }

    @Test
    void putRoutes_OneOfTwoExists_OverwriteTrue_OneRouteAddedOtherRouteModified() throws IOException {
        ConfigRepository configRepository = repository();

        Route route1 = new Route().setPath(PATH).setMethod(RequestMethod.GET);
        Route route2 = new Route().setPath(PATH).setMethod(RequestMethod.POST).setResponse(STR1);
        configRepository.putRoutes(List.of(route1, route2), true);

        Route routeFound = configRepository.findRoute(route2).orElse(null);
        assertNotNull(routeFound);

        Route route3 = new Route().setPath(PATH).setMethod(RequestMethod.DELETE);
        Route route4 = new Route().setPath(PATH).setMethod(RequestMethod.POST).setResponse(STR2);
        configRepository.putRoutes(List.of(route3, route4), true);

        assertEquals(3, configRepository.findAllRoutes().size());
        Route route4Found = configRepository.findRoute(route4).orElse(null);
        assertNotNull(route4Found);
        assertEquals(STR2, route4Found.getResponse());
    }

    @Test
    void putRoutes_RouteExists_OverwriteFalse_RouteNotModified_DoesNotThrow() throws IOException {
        ConfigRepository configRepository = repository();
        Route route2 = new Route().setPath(PATH).setMethod(RequestMethod.POST).setResponse(STR1);
        configRepository.putRoutes(List.of(route2), true);
        Route route4 = new Route().setPath(PATH).setMethod(RequestMethod.POST).setResponse(STR2);

        assertDoesNotThrow(() -> configRepository.putRoutes(List.of(route4), false));

        Route route4Found = configRepository.findRoute(route4).orElse(null);

        assertNotNull(route4Found);
        assertEquals(STR1, route4Found.getResponse());
    }

    @Test
    void deleteRoutes_AddThenDeleteSameRoute_NoRoutesFound() throws IOException {
        ConfigRepository configRepository = repository();

        Route route1 = new Route().setPath(PATH);
        configRepository.putRoute(null, route1);

        assertEquals(1, configRepository.findAllRoutes().size());

        Route route2 = new Route().setPath(PATH);
        configRepository.deleteRoutes(List.of(route2));

        assertEquals(0, configRepository.findAllRoutes().size());
    }

    @Test
    void deleteRoutes_AddThenDeleteSameRoute_ListenerCalled() throws IOException {
        ConfigRepository configRepository = repository();

        Route route1 = new Route().setPath(PATH);
        configRepository.putRoute(null, route1);

        assertEquals(1, configRepository.findAllRoutes().size());

        Route route2 = new Route(route1);
        configRepository.deleteRoutes(List.of(route2));

        verify(routeObserver, times(1)).onRouteDeleted(any());
    }

    @Test
    void deleteRoutes_AddThenDeleteDifferentRoute_OneRouteFound() throws IOException {
        ConfigRepository configRepository = repository();

        Route route1 = new Route().setPath(PATH).setMethod(RequestMethod.GET);
        configRepository.putRoute(null, route1);

        assertEquals(1, configRepository.findAllRoutes().size());

        Route route2 = new Route().setPath(PATH).setMethod(RequestMethod.DELETE);
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
    void putScenario_NotExisting_Success() throws IOException {
        ConfigRepository configRepository = repository();

        Scenario scenario = new Scenario().setAlias(STR1);
        configRepository.putScenario(null, scenario);

        assertEquals(1, configRepository.findAllScenarios().size());
    }

    @Test
    void putScenario_UpdateExisting_UpdatesScenario() throws IOException {
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
    void putScenario_NoOriginalAndTryCreatingCopy_Throws() throws IOException {
        ConfigRepository configRepository = repository();
        Scenario scenario1 = new Scenario().setAlias(STR1).setData(STR1);
        Scenario scenario2 = new Scenario().setAlias(STR1).setData(STR2);
        configRepository.putScenario(null, scenario1);
        assertThrows(ScenarioAlreadyExistsException.class, () -> configRepository.putScenario(null, scenario2));
    }

    @Test
    void putScenario_OriginalProvidedAndTryCreatingCopy_Throws() throws IOException {
        ConfigRepository configRepository = repository();
        Scenario scenario1 = new Scenario().setAlias(STR1);
        Scenario scenario2 = new Scenario().setAlias(STR2);
        Scenario scenario3 = new Scenario().setAlias(STR2);
        configRepository.putScenario(null, scenario1);
        configRepository.putScenario(null, scenario2);
        assertThrows(ScenarioAlreadyExistsException.class, () -> configRepository.putScenario(scenario1, scenario3));
    }

    @Test
    void deleteScenario_AddThenDeleteSameScenario_NoScenariosFound() throws IOException {
        ConfigRepository configRepository = repository();

        Scenario scenario1 = new Scenario().setAlias(STR1);
        configRepository.putScenario(null, scenario1);
        configRepository.deleteScenario(scenario1);

        assertEquals(0, configRepository.findAllScenarios().size());
    }

    @Test
    void deleteScenario_WithNoObservers_DoesNotThrow() {
        ConfigRepository configRepository = repositoryWithNoObservers();

        Scenario scenario1 = new Scenario().setAlias(STR1);
        assertDoesNotThrow(() -> configRepository.putScenario(null, scenario1));
        assertDoesNotThrow(() -> configRepository.deleteScenario(scenario1));
    }

    @Test
    void deleteScenario_AddThenDeleteDifferentScenario_OneScenarioFound() throws IOException {
        ConfigRepository configRepository = repository();

        Scenario scenario1 = new Scenario().setAlias(STR1);
        configRepository.putScenario(null, scenario1);

        Scenario scenario2 = new Scenario().setAlias(STR2);
        configRepository.deleteScenario(scenario2);

        assertEquals(1, configRepository.findAllScenarios().size());
    }

    //----------------------------------------------------------------------
    //
    //
    //
    //   Requests
    //
    //
    //
    //----------------------------------------------------------------------

    @Test
    void putRequest_NonExisting_RequestAdded() throws IOException {
        ConfigRepository configRepository = repository();
        var v = new OutboundRequest().setId(STR1);
        configRepository.putRequest(null, v);

        var list = configRepository.findAllRequests();
        assertEquals(1, list.size());
        assertEquals(v, list.get(0));
    }

    @DisplayName("Update existing Request [1] with contents of [2], success")
    @Test
    void putRequest_UpdateExisting_Updated() throws IOException {
        ConfigRepository configRepository = repository();
        var v1 = new OutboundRequest().setId(STR1);
        var v2 = new OutboundRequest().setId(STR2);
        configRepository.putRequest(null, v1);
        configRepository.putRequest(v1, v2);

        var optional = configRepository.findRequest(STR2);
        assertTrue(optional.isPresent());
        assertEquals(STR2, optional.get().getId());
    }

    @DisplayName("Try to create new Request [2] that equals already existing Request [1]")
    @Test
    void putRequest_ExistsByEquals_NewIdGenerated() throws IOException {
        ConfigRepository configRepository = repository();
        var v1 = new OutboundRequest().setId(STR1).setPath(STR1);
        var v2 = new OutboundRequest().setId(STR1).setPath(STR2);
        configRepository.putRequest(null, v1);
        configRepository.putRequest(null, v2);


        assertEquals(2, configRepository.findAllRequests().size());
        assertNotEquals(STR1, v2.getId());
        var optional = configRepository.findRequest(v2.getId());
        assertTrue(optional.isPresent());
    }

    @Test
    void putRequest_ExistsByEqualsAndPathIsSame_NewIdGeneratedWithIndex() throws IOException {
        ConfigRepository configRepository = repository();
        var v1 = new OutboundRequest().setPath(STR1);
        var v2 = new OutboundRequest().setPath(STR1);
        configRepository.putRequest(null, v1);
        configRepository.putRequest(null, v2);

        assertEquals(2, configRepository.findAllRequests().size());
        assertNotEquals(STR1, v2.getId());
        var optional = configRepository.findRequest(v2.getId());
        assertTrue(optional.isPresent());
        assertTrue(optional.get().getId().endsWith("1"));
    }

    @Test
    void putRoutes_NonExisting_Added() throws IOException {
        ConfigRepository configRepository = repository();

        var v1 = new OutboundRequest().setId(STR1);
        var v2 = new OutboundRequest().setId(STR2);
        configRepository.putRequests(List.of(v1, v2), true);

        var list = configRepository.findAllRequests();
        assertEquals(2, list.size());
    }

    @Test
    void createRequest_ThenDeleteSameRequest_NoRequestsFound() throws IOException {
        ConfigRepository configRepository = repository();
        var v1 = new OutboundRequest().setId(STR1);
        var v2 = new OutboundRequest().setId(STR1);
        configRepository.putRequest(null, v1);

        assertEquals(1, configRepository.findAllRequests().size());
        configRepository.deleteRequests(List.of(v2));
        assertEquals(0, configRepository.findAllRequests().size());
    }

    @Test
    void createRequest_ThenDeleteDifferentRequest_OneRequestFound() throws IOException {
        ConfigRepository configRepository = repository();
        var v1 = new OutboundRequest().setId(STR1);
        var v2 = new OutboundRequest().setId(STR2);
        configRepository.putRequest(null, v1);

        assertEquals(1, configRepository.findAllRequests().size());
        configRepository.deleteRequests(List.of(v2));
        assertEquals(1, configRepository.findAllRequests().size());
    }
}
