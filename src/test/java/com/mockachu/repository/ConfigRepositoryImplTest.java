package com.mockachu.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.mockachu.domain.*;
import com.mockachu.exception.KafkaTopicAlreadyExistsException;
import com.mockachu.exception.RouteAlreadyExistsException;
import com.mockachu.exception.ScenarioAlreadyExistsException;
import com.mockachu.exception.TestAlreadyExistsException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigRepositoryImplTest {

    private static final String STR1 = "AAA";
    private static final String STR2 = "BBB";
    private static final String PATH = "/test";

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
                getYamlMapper());

        configRepository.registerConfigObserver(configObserver);
        configRepository.registerRouteObserver(routeObserver);

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
    void putRoute_NonExisting_RouteAdded() throws IOException {
        var configRepository = repository();
        var route = new Route().setPath(PATH);
        configRepository.putRoute(null, route);

        var allRoutes = configRepository.findAllRoutes();
        assertEquals(1, allRoutes.size());
        assertEquals(route, allRoutes.get(0));
    }

    @Test
    void putRoute_NonExisting_ListenerCalled() throws IOException {
        var configRepository = repository();
        var route = new Route().setPath(PATH);
        configRepository.putRoute(null, route);

        verify(routeObserver, times(1)).onRouteCreated(any());
    }

    @DisplayName("Update existing [1] with contents of [2], update successful")
    @Test
    void putRoute_UpdateExisting_RouteUpdated() throws IOException {
        var configRepository = repository();
        var route1 = new Route().setMethod(RequestMethod.GET).setPath(PATH).setResponse(STR1);
        var route2 = new Route().setMethod(RequestMethod.GET).setPath(PATH).setResponse(STR2);
        configRepository.putRoute(null, route1);
        configRepository.putRoute(route1, route2);

        var routeSearch = new Route().setPath(PATH);
        var routeOptional = configRepository.findRoute(routeSearch);
        assertTrue(routeOptional.isPresent());
        assertEquals(STR2, routeOptional.get().getResponse());
    }

    @DisplayName("Update existing [1] with contents of [2], listener called")
    @Test
    void putRoute_UpdateExisting_ListenersCalled() throws IOException {
        var configRepository = repository();

        var route1 = new Route().setMethod(RequestMethod.GET).setPath(PATH).setResponse(STR1);
        var route2 = new Route().setMethod(RequestMethod.GET).setPath(PATH).setResponse(STR2);

        configRepository.putRoute(null, route1);
        verify(routeObserver, times(1)).onRouteCreated(any());
        verify(routeObserver, never()).onRouteDeleted(any());

        configRepository.putRoute(route1, route2);
        verify(routeObserver, times(2)).onRouteCreated(any());
        verify(routeObserver, times(1)).onRouteDeleted(any());
    }

    @Test
    void putRoute_WithNoObservers_DoesNotThrow() {
        var configRepository = repositoryWithNoObservers();

        var route1 = new Route().setMethod(RequestMethod.GET).setPath(PATH).setResponse(STR1);
        var route2 = new Route().setMethod(RequestMethod.GET).setPath(PATH).setResponse(STR2);

        assertDoesNotThrow(() -> configRepository.putRoute(null, route1));
        assertDoesNotThrow(() -> configRepository.putRoute(route1, route2));
    }

    @DisplayName("Try to create new route [2] that equals already existing route [1] -> throws (do not allow duplicates)")
    @Test
    void putRoute_NewExistsByEquals_Throws() throws IOException {
        var configRepository = repository();
        var route1 = new Route().setMethod(RequestMethod.GET).setPath(PATH).setResponse(STR1);
        var route2 = new Route().setMethod(RequestMethod.GET).setPath(PATH).setResponse(STR2);
        configRepository.putRoute(null, route1);
        assertThrows(RouteAlreadyExistsException.class, () -> configRepository.putRoute(null, route2));
    }

    @DisplayName("Update existing route [1] with contents of [3] which equals other existing route [2] -> throws (do not allow duplicates)")
    @Test
    void putRoute_ReferenceExistsAndNewExistsAndReferenceNotEqualsNew_Throws() throws IOException {
        var configRepository = repository();
        var route1 = new Route().setMethod(RequestMethod.GET).setPath(PATH).setResponse(STR1);
        var route2 = new Route().setMethod(RequestMethod.POST).setPath(PATH).setResponse(STR1);
        var route3 = new Route().setMethod(RequestMethod.POST).setPath(PATH).setResponse(STR2);
        configRepository.putRoute(null, route1);
        configRepository.putRoute(null, route2);
        assertThrows(RouteAlreadyExistsException.class, () -> configRepository.putRoute(route1, route3));
    }

    @Test
    void putRoutes_NonExisting_RoutesAdded() throws IOException {
        var configRepository = repository();

        var route1 = new Route().setPath(PATH).setMethod(RequestMethod.GET);
        var route2 = new Route().setPath(PATH).setMethod(RequestMethod.POST);
        configRepository.putRoutes(List.of(route1, route2), true);

        var allRoutes = configRepository.findAllRoutes();
        assertEquals(2, allRoutes.size());
    }

    @Test
    void putRoutes_NonExisting_ListenerCalled() throws IOException {
        var configRepository = repository();

        var route1 = new Route().setPath(PATH).setMethod(RequestMethod.GET);
        var route2 = new Route().setPath(PATH).setMethod(RequestMethod.POST);
        configRepository.putRoutes(List.of(route1, route2), true);

        verify(routeObserver, times(2)).onRouteCreated(any());
    }

    @Test
    void putRoutes_OneOfTwoExists_OverwriteTrue_OneRouteAddedOtherRouteModified() throws IOException {
        var configRepository = repository();

        var route1 = new Route().setPath(PATH).setMethod(RequestMethod.GET);
        var route2 = new Route().setPath(PATH).setMethod(RequestMethod.POST).setResponse(STR1);
        configRepository.putRoutes(List.of(route1, route2), true);

        var routeFound = configRepository.findRoute(route2).orElse(null);
        assertNotNull(routeFound);

        var route3 = new Route().setPath(PATH).setMethod(RequestMethod.DELETE);
        var route4 = new Route().setPath(PATH).setMethod(RequestMethod.POST).setResponse(STR2);
        configRepository.putRoutes(List.of(route3, route4), true);

        assertEquals(3, configRepository.findAllRoutes().size());
        var route4Found = configRepository.findRoute(route4).orElse(null);
        assertNotNull(route4Found);
        assertEquals(STR2, route4Found.getResponse());
    }

    @Test
    void putRoutes_RouteExists_OverwriteFalse_RouteNotModified_DoesNotThrow() throws IOException {
        var configRepository = repository();
        var route2 = new Route().setPath(PATH).setMethod(RequestMethod.POST).setResponse(STR1);
        configRepository.putRoutes(List.of(route2), true);
        var route4 = new Route().setPath(PATH).setMethod(RequestMethod.POST).setResponse(STR2);

        assertDoesNotThrow(() -> configRepository.putRoutes(List.of(route4), false));

        var route4Found = configRepository.findRoute(route4).orElse(null);

        assertNotNull(route4Found);
        assertEquals(STR1, route4Found.getResponse());
    }

    @Test
    void deleteRoutes_AddThenDeleteSameRoute_NoRoutesFound() throws IOException {
        var configRepository = repository();

        var route1 = new Route().setPath(PATH);
        configRepository.putRoute(null, route1);

        assertEquals(1, configRepository.findAllRoutes().size());

        var route2 = new Route().setPath(PATH);
        configRepository.deleteRoutes(List.of(route2));

        assertEquals(0, configRepository.findAllRoutes().size());
    }

    @Test
    void deleteRoutes_AddThenDeleteSameRoute_ListenerCalled() throws IOException {
        var configRepository = repository();

        var route1 = new Route().setPath(PATH);
        configRepository.putRoute(null, route1);

        assertEquals(1, configRepository.findAllRoutes().size());

        var route2 = new Route(route1);
        configRepository.deleteRoutes(List.of(route2));

        verify(routeObserver, times(1)).onRouteDeleted(any());
    }

    @Test
    void deleteRoutes_AddThenDeleteDifferentRoute_OneRouteFound() throws IOException {
        var configRepository = repository();

        var route1 = new Route().setPath(PATH).setMethod(RequestMethod.GET);
        configRepository.putRoute(null, route1);

        assertEquals(1, configRepository.findAllRoutes().size());

        var route2 = new Route().setPath(PATH).setMethod(RequestMethod.DELETE);
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
    void putRequests_NonExistingOneIsDuplicate_TwoAddedOneOverwritten() throws IOException {
        ConfigRepository configRepository = repository();

        var v1 = new OutboundRequest().setId(STR1);
        var v2 = new OutboundRequest().setId(STR2);
        var v3 = new OutboundRequest().setId(STR1);
        configRepository.putRequests(List.of(v1, v2, v3), true);

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

    //----------------------------------------------------------------------
    //
    //
    //
    //   Tests
    //
    //
    //
    //----------------------------------------------------------------------

    @Test
    void putTest_NonExisting_TestAdded() throws IOException {
        ConfigRepository configRepository = repository();
        var v = new ApiTest().setAlias(STR1);
        configRepository.putTest(null, v);

        var list = configRepository.findAllTests();
        assertEquals(1, list.size());
        assertEquals(v, list.get(0));
    }

    @DisplayName("Update existing Test [1] with contents of [2], success")
    @Test
    void putTest_UpdateExisting_Updated() throws IOException {
        ConfigRepository configRepository = repository();
        var v1 = new ApiTest().setAlias(STR1);
        var v2 = new ApiTest().setAlias(STR2);
        configRepository.putTest(null, v1);
        configRepository.putTest(v1, v2);

        var optional = configRepository.findTest(STR2);
        assertTrue(optional.isPresent());
        assertEquals(STR2, optional.get().getAlias());
    }

    @DisplayName("Try to create new Test [2] that equals already existing Test [1]")
    @Test
    void putTest_ExistsByEquals_ExceptionThrows() throws IOException {
        ConfigRepository configRepository = repository();
        var v1 = new ApiTest().setAlias(STR1).setGroup(STR1);
        var v2 = new ApiTest().setAlias(STR1).setGroup(STR2);
        configRepository.putTest(null, v1);

        assertThrows(TestAlreadyExistsException.class, () -> configRepository.putTest(null, v2));
    }

    @Test
    void putTests_NonExistingOneIsDuplicate_TwoAddedOneOverwritten() throws IOException {
        ConfigRepository configRepository = repository();

        var v1 = new ApiTest().setAlias(STR1);
        var v2 = new ApiTest().setAlias(STR2);
        var v3 = new ApiTest().setAlias(STR1);
        configRepository.putTests(List.of(v1, v2, v3), true);

        var list = configRepository.findAllTests();
        assertEquals(2, list.size());
    }

    @Test
    void createTest_ThenDeleteSameTest_NoTestsFound() throws IOException {
        ConfigRepository configRepository = repository();
        var v1 = new ApiTest().setAlias(STR1);
        var v2 = new ApiTest().setAlias(STR1);
        configRepository.putTest(null, v1);

        assertEquals(1, configRepository.findAllTests().size());
        configRepository.deleteTests(List.of(v2));
        assertEquals(0, configRepository.findAllTests().size());
    }

    @Test
    void createTest_ThenDeleteDifferentTest_OneTestFound() throws IOException {
        ConfigRepository configRepository = repository();
        var v1 = new ApiTest().setAlias(STR1);
        var v2 = new ApiTest().setAlias(STR2);
        configRepository.putTest(null, v1);

        assertEquals(1, configRepository.findAllTests().size());
        configRepository.deleteTests(List.of(v2));
        assertEquals(1, configRepository.findAllTests().size());
    }

    //----------------------------------------------------------------------
    //
    //
    //
    //   Kafka Topics
    //
    //
    //
    //----------------------------------------------------------------------

    @Test
    void putKafkaTopic_NonExisting_Added() throws IOException {
        ConfigRepository configRepository = repository();
        var entity = new KafkaTopic().setTopic("AAA");
        configRepository.putKafkaTopic(null, entity);

        var list = configRepository.findAllKafkaTopics();
        assertEquals(1, list.size());
        assertEquals(entity, list.get(0));
    }

    @DisplayName("Update existing [1] with contents of [2], update successful")
    @Test
    void putKafkaTopic_UpdateExisting_Updated() throws IOException {
        var configRepository = repository();
        var entity1 = new KafkaTopic().setGroup("AAA").setTopic("AAA").setPartition(0);
        var entity2 = new KafkaTopic().setGroup("AAA").setTopic("AAA").setPartition(1);
        configRepository.putKafkaTopic(null, entity1);
        configRepository.putKafkaTopic(entity1, entity2);

        var optional = configRepository.findKafkaTopic("AAA", 1);
        assertTrue(optional.isPresent());
        assertEquals(1, optional.get().getPartition());
    }

    @DisplayName("Try to create new [2] that equals already existing [1] -> throws (do not allow duplicates)")
    @Test
    void putKafkaTopic_NewExistsByEquals_Throws() throws IOException {
        var configRepository = repository();
        var entity1 = new KafkaTopic().setGroup("AAA").setTopic("AAA").setPartition(0);
        var entity2 = new KafkaTopic().setGroup("AAA").setTopic("AAA").setPartition(0);
        configRepository.putKafkaTopic(null, entity1);
        assertThrows(KafkaTopicAlreadyExistsException.class, () -> configRepository.putKafkaTopic(null, entity2));
    }

    @DisplayName("Update existing [1] with contents of [3] which equals other existing [2] -> throws (do not allow duplicates)")
    @Test
    void putKafkaTopic_ReferenceExistsAndNewExistsAndReferenceNotEqualsNew_Throws() throws IOException {
        var configRepository = repository();
        var entity1 = new KafkaTopic().setGroup("AAA").setTopic("AAA").setPartition(0);
        var entity2 = new KafkaTopic().setGroup("AAA").setTopic("BBB").setPartition(1);
        var entity3 = new KafkaTopic().setGroup("AAA").setTopic("BBB").setPartition(1);
        configRepository.putKafkaTopic(null, entity1);
        configRepository.putKafkaTopic(null, entity2);
        assertThrows(KafkaTopicAlreadyExistsException.class, () -> configRepository.putKafkaTopic(entity1, entity3));
    }

    @Test
    void putKafkaTopics_NonExisting_AllAdded() throws IOException {
        var configRepository = repository();

        var entity1 = new KafkaTopic().setGroup("AAA").setTopic("AAA").setPartition(0);
        var entity2 = new KafkaTopic().setGroup("BBB").setTopic("BBB").setPartition(0);
        configRepository.putKafkaTopics(List.of(entity1, entity2), true);

        var all = configRepository.findAllKafkaTopics();
        assertEquals(2, all.size());
    }

    @Test
    void putKafkaTopics_OneOfTwoExists_OverwriteTrue_OneAddedOtherModified() throws IOException {
        var configRepository = repository();

        var entity1 = new KafkaTopic().setGroup("AAA").setTopic("AAA").setPartition(0);
        var entity2 = new KafkaTopic().setGroup("BBB").setTopic("BBB").setPartition(0);
        configRepository.putKafkaTopics(List.of(entity1, entity2), true);

        var found = configRepository.findKafkaTopic("BBB", 0).orElse(null);
        assertNotNull(found);

        var entity3 = new KafkaTopic().setGroup("CCC").setTopic("CCC").setPartition(0);
        var entity4 = new KafkaTopic().setGroup("BBB").setTopic("BBB").setPartition(0);
        configRepository.putKafkaTopics(List.of(entity3, entity4), true);

        assertEquals(3, configRepository.findAllKafkaTopics().size());
        found = configRepository.findKafkaTopic("BBB", 0).orElse(null);
        assertNotNull(found);
        assertEquals("BBB", found.getTopic());
    }

    @Test
    void putKafkaTopics_RouteExists_OverwriteFalse_RouteNotModified_DoesNotThrow() throws IOException {
        var configRepository = repository();
        var entity2 = new KafkaTopic().setGroup("BBB").setTopic("BBB").setPartition(0);
        configRepository.putKafkaTopics(List.of(entity2), true);
        var entity4 = new KafkaTopic().setGroup("BBB").setTopic("BBB").setPartition(0);

        assertDoesNotThrow(() -> configRepository.putKafkaTopics(List.of(entity4), false));

        var found = configRepository.findKafkaTopic("BBB", 0).orElse(null);

        assertNotNull(found);
        assertEquals("BBB", found.getTopic());
    }

    @Test
    void deleteKafkaTopics_AddThenDeleteSameRoute_NoRoutesFound() throws IOException {
        var configRepository = repository();

        var entity1 = new KafkaTopic().setTopic("BBB");
        configRepository.putKafkaTopic(null, entity1);

        assertEquals(1, configRepository.findAllKafkaTopics().size());

        var entity2 = new KafkaTopic().setTopic("BBB");
        configRepository.deleteKafkaTopics(List.of(entity2));

        assertEquals(0, configRepository.findAllKafkaTopics().size());
    }

    @Test
    void deleteKafkaTopics_AddThenDeleteDifferentOne_OneFound() throws IOException {
        var configRepository = repository();

        var entity1 = new KafkaTopic().setTopic("AAA");
        configRepository.putKafkaTopic(null, entity1);

        assertEquals(1, configRepository.findAllKafkaTopics().size());

        var entity2 = new KafkaTopic().setTopic("BBB");
        configRepository.deleteKafkaTopics(List.of(entity2));

        assertEquals(1, configRepository.findAllKafkaTopics().size());
    }
}
