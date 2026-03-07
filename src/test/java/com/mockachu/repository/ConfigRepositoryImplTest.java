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
        var repo = new ConfigRepositoryImpl(
                getTempFile("config.yml"),
                getTempFile("backup.yml"),
                getYamlMapper());

        repo.registerConfigObserver(configObserver);
        repo.registerRouteObserver(routeObserver);
        repo.setAutoSave(false);

        return repo;
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

    @Test
    void autoSaveEnabledByDefault() {
        var repo = new ConfigRepositoryImpl(
                null, null, getYamlMapper());
        assertTrue(repo.isAutoSave());
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
        var repo = repository();
        assertNotEquals("", repo.getConfigData());
    }

    @Test
    void writeConfigData_DefaultConfig_ListenersCalled() throws IOException {
        var repo = repository();

        String configData = repo.getConfigData();
        repo.writeConfigData(configData);

        verify(configObserver, times(1)).onBeforeConfigChanged();
        verify(configObserver, times(1)).onAfterConfigChanged();
    }

    @Test
    void writeConfigData_NoObservers_DoesNotThrow() throws IOException {
        var repo = repositoryWithNoObservers();
        String configData = repo.getConfigData();

        assertDoesNotThrow(() -> repo.writeConfigData(configData));
    }

    @Test
    void backupAndRestore_NonDefaultConfig_ConfigRestored() throws IOException {
        var repo = repository();
        repo.setAutoSave(true);

        var route = new Route().setPath(PATH);
        repo.putRoute(null, route);

        String configData = repo.getConfigData();

        repo.backup();
        repo.deleteRoutes(List.of(route));
        repo.restore();

        assertEquals(configData, repo.getConfigData());
    }

    @Test
    void restore_ListenersCalled() throws IOException {
        var repo = repository();
        repo.setAutoSave(true);

        repo.backup();
        repo.restore();

        verify(configObserver, times(1)).onBeforeConfigChanged();
        verify(configObserver, times(1)).onAfterConfigChanged();
    }

    @Test
    void writeConfigData_WritePreviouslyReadData_ConfigRestored() throws IOException {
        var repo = repository();
        var route = new Route().setPath(PATH);
        repo.putRoute(null, route);
        String configData = repo.getConfigData();

        repo = repository();
        repo.writeConfigData(configData);

        assertTrue(repo.findRoute(route).isPresent());
    }

    @Test
    void writeConfigData_EmptyString_Throws() {
        var repo = repository();

        assertThrows(IOException.class, () -> repo.writeConfigData(""));
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
        var repo = repository();
        assertNotNull(repo.getSettings());
    }

    @Test
    void setSettings_NonDefaultSettings_ReturnsEqualSettings() throws IOException {
        var repo = repository();

        Settings settings = new Settings();
        boolean expected = !settings.isQuantum();
        settings.setQuantum(expected);
        repo.setSettings(settings);

        assertEquals(expected, repo.getSettings().isQuantum());
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
        var repo = repository();
        var route = new Route().setPath(PATH);
        repo.putRoute(null, route);

        var allRoutes = repo.findAllRoutes();
        assertEquals(1, allRoutes.size());
        assertEquals(route, allRoutes.get(0));
    }

    @Test
    void putRoute_NonExisting_ListenerCalled() throws IOException {
        var repo = repository();
        var route = new Route().setPath(PATH);
        repo.putRoute(null, route);

        verify(routeObserver, times(1)).onRouteCreated(any());
    }

    @DisplayName("Update existing [1] with contents of [2], update successful")
    @Test
    void putRoute_UpdateExisting_RouteUpdated() throws IOException {
        var repo = repository();
        var route1 = new Route().setMethod(RequestMethod.GET).setPath(PATH).setResponse(STR1);
        var route2 = new Route().setMethod(RequestMethod.GET).setPath(PATH).setResponse(STR2);
        repo.putRoute(null, route1);
        repo.putRoute(route1, route2);

        var routeSearch = new Route().setPath(PATH);
        var routeOptional = repo.findRoute(routeSearch);
        assertTrue(routeOptional.isPresent());
        assertEquals(STR2, routeOptional.get().getResponse());
    }

    @DisplayName("Update existing [1] with contents of [2], listener called")
    @Test
    void putRoute_UpdateExisting_ListenersCalled() throws IOException {
        var repo = repository();

        var route1 = new Route().setMethod(RequestMethod.GET).setPath(PATH).setResponse(STR1);
        var route2 = new Route().setMethod(RequestMethod.GET).setPath(PATH).setResponse(STR2);

        repo.putRoute(null, route1);
        verify(routeObserver, times(1)).onRouteCreated(any());
        verify(routeObserver, never()).onRouteDeleted(any());

        repo.putRoute(route1, route2);
        verify(routeObserver, times(2)).onRouteCreated(any());
        verify(routeObserver, times(1)).onRouteDeleted(any());
    }

    @Test
    void putRoute_WithNoObservers_DoesNotThrow() {
        var repo = repositoryWithNoObservers();

        var route1 = new Route().setMethod(RequestMethod.GET).setPath(PATH).setResponse(STR1);
        var route2 = new Route().setMethod(RequestMethod.GET).setPath(PATH).setResponse(STR2);

        assertDoesNotThrow(() -> repo.putRoute(null, route1));
        assertDoesNotThrow(() -> repo.putRoute(route1, route2));
    }

    @DisplayName("Try to create new route [2] that equals already existing route [1] -> throws (do not allow duplicates)")
    @Test
    void putRoute_NewExistsByEquals_Throws() throws IOException {
        var repo = repository();
        var route1 = new Route().setMethod(RequestMethod.GET).setPath(PATH).setResponse(STR1);
        var route2 = new Route().setMethod(RequestMethod.GET).setPath(PATH).setResponse(STR2);
        repo.putRoute(null, route1);
        assertThrows(RouteAlreadyExistsException.class, () -> repo.putRoute(null, route2));
    }

    @DisplayName("Update existing route [1] with contents of [3] which equals other existing route [2] -> throws (do not allow duplicates)")
    @Test
    void putRoute_ReferenceExistsAndNewExistsAndReferenceNotEqualsNew_Throws() throws IOException {
        var repo = repository();
        var route1 = new Route().setMethod(RequestMethod.GET).setPath(PATH).setResponse(STR1);
        var route2 = new Route().setMethod(RequestMethod.POST).setPath(PATH).setResponse(STR1);
        var route3 = new Route().setMethod(RequestMethod.POST).setPath(PATH).setResponse(STR2);
        repo.putRoute(null, route1);
        repo.putRoute(null, route2);
        assertThrows(RouteAlreadyExistsException.class, () -> repo.putRoute(route1, route3));
    }

    @Test
    void putRoutes_NonExisting_RoutesAdded() throws IOException {
        var repo = repository();

        var route1 = new Route().setPath(PATH).setMethod(RequestMethod.GET);
        var route2 = new Route().setPath(PATH).setMethod(RequestMethod.POST);
        repo.putRoutes(List.of(route1, route2), true);

        var allRoutes = repo.findAllRoutes();
        assertEquals(2, allRoutes.size());
    }

    @Test
    void putRoutes_NonExisting_ListenerCalled() throws IOException {
        var repo = repository();

        var route1 = new Route().setPath(PATH).setMethod(RequestMethod.GET);
        var route2 = new Route().setPath(PATH).setMethod(RequestMethod.POST);
        repo.putRoutes(List.of(route1, route2), true);

        verify(routeObserver, times(2)).onRouteCreated(any());
    }

    @Test
    void putRoutes_OneOfTwoExists_OverwriteTrue_OneRouteAddedOtherRouteModified() throws IOException {
        var repo = repository();

        var route1 = new Route().setPath(PATH).setMethod(RequestMethod.GET);
        var route2 = new Route().setPath(PATH).setMethod(RequestMethod.POST).setResponse(STR1);
        repo.putRoutes(List.of(route1, route2), true);

        var routeFound = repo.findRoute(route2).orElse(null);
        assertNotNull(routeFound);

        var route3 = new Route().setPath(PATH).setMethod(RequestMethod.DELETE);
        var route4 = new Route().setPath(PATH).setMethod(RequestMethod.POST).setResponse(STR2);
        repo.putRoutes(List.of(route3, route4), true);

        assertEquals(3, repo.findAllRoutes().size());
        var route4Found = repo.findRoute(route4).orElse(null);
        assertNotNull(route4Found);
        assertEquals(STR2, route4Found.getResponse());
    }

    @Test
    void putRoutes_RouteExists_OverwriteFalse_RouteNotModified_DoesNotThrow() throws IOException {
        var repo = repository();
        var route2 = new Route().setPath(PATH).setMethod(RequestMethod.POST).setResponse(STR1);
        repo.putRoutes(List.of(route2), true);
        var route4 = new Route().setPath(PATH).setMethod(RequestMethod.POST).setResponse(STR2);

        assertDoesNotThrow(() -> repo.putRoutes(List.of(route4), false));

        var route4Found = repo.findRoute(route4).orElse(null);

        assertNotNull(route4Found);
        assertEquals(STR1, route4Found.getResponse());
    }

    @Test
    void deleteRoutes_AddThenDeleteSameRoute_NoRoutesFound() throws IOException {
        var repo = repository();

        var route1 = new Route().setPath(PATH);
        repo.putRoute(null, route1);

        assertEquals(1, repo.findAllRoutes().size());

        var route2 = new Route().setPath(PATH);
        repo.deleteRoutes(List.of(route2));

        assertEquals(0, repo.findAllRoutes().size());
    }

    @Test
    void deleteRoutes_AddThenDeleteSameRoute_ListenerCalled() throws IOException {
        var repo = repository();

        var route1 = new Route().setPath(PATH);
        repo.putRoute(null, route1);

        assertEquals(1, repo.findAllRoutes().size());

        var route2 = new Route(route1);
        repo.deleteRoutes(List.of(route2));

        verify(routeObserver, times(1)).onRouteDeleted(any());
    }

    @Test
    void deleteRoutes_AddThenDeleteDifferentRoute_OneRouteFound() throws IOException {
        var repo = repository();

        var route1 = new Route().setPath(PATH).setMethod(RequestMethod.GET);
        repo.putRoute(null, route1);

        assertEquals(1, repo.findAllRoutes().size());

        var route2 = new Route().setPath(PATH).setMethod(RequestMethod.DELETE);
        repo.deleteRoutes(List.of(route2));

        assertEquals(1, repo.findAllRoutes().size());
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
        var repo = repository();

        var entity = new Scenario().setAlias(STR1);
        repo.putScenario(null, entity);

        assertEquals(1, repo.findAllScenarios().size());
    }

    @Test
    void putScenario_UpdateExisting_UpdatesScenario() throws IOException {
        var repo = repository();
        var entity1 = new Scenario().setAlias(STR1).setData(STR1);
        var entity2 = new Scenario().setAlias(STR1).setData(STR2);
        repo.putScenario(null, entity1);
        repo.putScenario(entity1, entity2);
        var list = repo.findAllScenarios();

        assertEquals(1, list.size());
        assertEquals(STR2, list.get(0).getData());
    }

    @Test
    void putScenario_NoOriginalAndTryCreatingCopy_Throws() throws IOException {
        var repo = repository();
        var entity1 = new Scenario().setAlias(STR1).setData(STR1);
        var entity2 = new Scenario().setAlias(STR1).setData(STR2);
        repo.putScenario(null, entity1);
        assertThrows(ScenarioAlreadyExistsException.class, () -> repo.putScenario(null, entity2));
    }

    @Test
    void putScenario_OriginalProvidedAndTryCreatingCopy_Throws() throws IOException {
        var repo = repository();
        var entity1 = new Scenario().setAlias(STR1);
        var entity2 = new Scenario().setAlias(STR2);
        var entity3 = new Scenario().setAlias(STR2);
        repo.putScenario(null, entity1);
        repo.putScenario(null, entity2);
        assertThrows(ScenarioAlreadyExistsException.class, () -> repo.putScenario(entity1, entity3));
    }

    @Test
    void deleteScenario_AddThenDeleteSameScenario_NoScenariosFound() throws IOException {
        var repo = repository();

        var entity1 = new Scenario().setAlias(STR1);
        repo.putScenario(null, entity1);
        repo.deleteScenarios(List.of(entity1));

        assertEquals(0, repo.findAllScenarios().size());
    }

    @Test
    void deleteScenario_WithNoObservers_DoesNotThrow() {
        var repo = repositoryWithNoObservers();
        var entity = new Scenario().setAlias(STR1);
        assertDoesNotThrow(() -> repo.putScenario(null, entity));
        assertDoesNotThrow(() -> repo.deleteScenarios(List.of(entity)));
    }

    @Test
    void deleteScenario_AddThenDeleteDifferentScenario_OneScenarioFound() throws IOException {
        var repo = repository();

        var entity1 = new Scenario().setAlias(STR1);
        repo.putScenario(null, entity1);

        var entity2 = new Scenario().setAlias(STR2);
        repo.deleteScenarios(List.of(entity2));

        assertEquals(1, repo.findAllScenarios().size());
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
        var repo = repository();
        var entity = new OutboundRequest().setId(STR1);
        repo.putRequest(null, entity);

        var list = repo.findAllRequests();
        assertEquals(1, list.size());
        assertEquals(entity, list.get(0));
    }

    @DisplayName("Update existing Request [1] with contents of [2], success")
    @Test
    void putRequest_UpdateExisting_Updated() throws IOException {
        var repo = repository();
        var entity1 = new OutboundRequest().setId(STR1);
        var entity2 = new OutboundRequest().setId(STR2);
        repo.putRequest(null, entity1);
        repo.putRequest(entity1, entity2);

        var optional = repo.findRequest(STR2);
        assertTrue(optional.isPresent());
        assertEquals(STR2, optional.get().getId());
    }

    @DisplayName("Try to create new Request [2] that equals already existing Request [1]")
    @Test
    void putRequest_ExistsByEquals_NewIdGenerated() throws IOException {
        var repo = repository();
        var entity1 = new OutboundRequest().setId(STR1).setPath(STR1);
        var entity2 = new OutboundRequest().setId(STR1).setPath(STR2);
        repo.putRequest(null, entity1);
        repo.putRequest(null, entity2);


        assertEquals(2, repo.findAllRequests().size());
        assertNotEquals(STR1, entity2.getId());
        var optional = repo.findRequest(entity2.getId());
        assertTrue(optional.isPresent());
    }

    @Test
    void putRequest_ExistsByEqualsAndPathIsSame_NewIdGeneratedWithIndex() throws IOException {
        var repo = repository();
        var entity1 = new OutboundRequest().setPath(STR1);
        var entity2 = new OutboundRequest().setPath(STR1);
        repo.putRequest(null, entity1);
        repo.putRequest(null, entity2);

        assertEquals(2, repo.findAllRequests().size());
        assertNotEquals(STR1, entity2.getId());
        var optional = repo.findRequest(entity2.getId());
        assertTrue(optional.isPresent());
        assertTrue(optional.get().getId().endsWith("1"));
    }

    @Test
    void putRequests_NonExistingOneIsDuplicate_TwoAddedOneOverwritten() throws IOException {
        var repo = repository();

        var entity1 = new OutboundRequest().setId(STR1);
        var entity2 = new OutboundRequest().setId(STR2);
        var entity3 = new OutboundRequest().setId(STR1);
        repo.putRequests(List.of(entity1, entity2, entity3), true);

        var list = repo.findAllRequests();
        assertEquals(2, list.size());
    }

    @Test
    void createRequest_ThenDeleteSameRequest_NoRequestsFound() throws IOException {
        var repo = repository();
        var entity1 = new OutboundRequest().setId(STR1);
        var entity2 = new OutboundRequest().setId(STR1);
        repo.putRequest(null, entity1);

        assertEquals(1, repo.findAllRequests().size());
        repo.deleteRequests(List.of(entity2));
        assertEquals(0, repo.findAllRequests().size());
    }

    @Test
    void createRequest_ThenDeleteDifferentRequest_OneRequestFound() throws IOException {
        var repo = repository();
        var entity1 = new OutboundRequest().setId(STR1);
        var entity2 = new OutboundRequest().setId(STR2);
        repo.putRequest(null, entity1);

        assertEquals(1, repo.findAllRequests().size());
        repo.deleteRequests(List.of(entity2));
        assertEquals(1, repo.findAllRequests().size());
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
        var repo = repository();
        var entity = new ApiTest().setAlias(STR1);
        repo.putTest(null, entity);

        var list = repo.findAllTests();
        assertEquals(1, list.size());
        assertEquals(entity, list.get(0));
    }

    @DisplayName("Update existing Test [1] with contents of [2], success")
    @Test
    void putTest_UpdateExisting_Updated() throws IOException {
        var repo = repository();
        var entity1 = new ApiTest().setAlias(STR1);
        var entity2 = new ApiTest().setAlias(STR2);
        repo.putTest(null, entity1);
        repo.putTest(entity1, entity2);

        var optional = repo.findTest(STR2);
        assertTrue(optional.isPresent());
        assertEquals(STR2, optional.get().getAlias());
    }

    @DisplayName("Try to create new Test [2] that equals already existing Test [1]")
    @Test
    void putTest_ExistsByEquals_ExceptionThrows() throws IOException {
        var repo = repository();
        var entity1 = new ApiTest().setAlias(STR1).setGroup(STR1);
        var entity2 = new ApiTest().setAlias(STR1).setGroup(STR2);
        repo.putTest(null, entity1);

        assertThrows(TestAlreadyExistsException.class, () -> repo.putTest(null, entity2));
    }

    @Test
    void putTests_NonExistingOneIsDuplicate_TwoAddedOneOverwritten() throws IOException {
        var repo = repository();

        var entity1 = new ApiTest().setAlias(STR1);
        var entity2 = new ApiTest().setAlias(STR2);
        var entity3 = new ApiTest().setAlias(STR1);
        repo.putTests(List.of(entity1, entity2, entity3), true);

        var list = repo.findAllTests();
        assertEquals(2, list.size());
    }

    @Test
    void createTest_ThenDeleteSameTest_NoTestsFound() throws IOException {
        var repo = repository();
        var entity1 = new ApiTest().setAlias(STR1);
        var entity2 = new ApiTest().setAlias(STR1);
        repo.putTest(null, entity1);

        assertEquals(1, repo.findAllTests().size());
        repo.deleteTests(List.of(entity2));
        assertEquals(0, repo.findAllTests().size());
    }

    @Test
    void createTest_ThenDeleteDifferentTest_OneTestFound() throws IOException {
        var repo = repository();
        var entity1 = new ApiTest().setAlias(STR1);
        var entity2 = new ApiTest().setAlias(STR2);
        repo.putTest(null, entity1);

        assertEquals(1, repo.findAllTests().size());
        repo.deleteTests(List.of(entity2));
        assertEquals(1, repo.findAllTests().size());
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
        var repo = repository();
        var entity = new KafkaTopic().setTopic("AAA");
        repo.putKafkaTopic(null, entity);

        var list = repo.findAllKafkaTopics();
        assertEquals(1, list.size());
        assertEquals(entity, list.get(0));
    }

    @DisplayName("Update existing [1] with contents of [2], update successful")
    @Test
    void putKafkaTopic_UpdateExisting_Updated() throws IOException {
        var repo = repository();
        var entity1 = new KafkaTopic().setGroup("AAA").setTopic("AAA").setPartition(0);
        var entity2 = new KafkaTopic().setGroup("AAA").setTopic("AAA").setPartition(1);
        repo.putKafkaTopic(null, entity1);
        repo.putKafkaTopic(entity1, entity2);

        var optional = repo.findKafkaTopic("AAA", 1);
        assertTrue(optional.isPresent());
        assertEquals(1, optional.get().getPartition());
    }

    @DisplayName("Try to create new [2] that equals already existing [1] -> throws (do not allow duplicates)")
    @Test
    void putKafkaTopic_NewExistsByEquals_Throws() throws IOException {
        var repo = repository();
        var entity1 = new KafkaTopic().setGroup("AAA").setTopic("AAA").setPartition(0);
        var entity2 = new KafkaTopic().setGroup("AAA").setTopic("AAA").setPartition(0);
        repo.putKafkaTopic(null, entity1);
        assertThrows(KafkaTopicAlreadyExistsException.class, () -> repo.putKafkaTopic(null, entity2));
    }

    @DisplayName("Update existing [1] with contents of [3] which equals other existing [2] -> throws (do not allow duplicates)")
    @Test
    void putKafkaTopic_ReferenceExistsAndNewExistsAndReferenceNotEqualsNew_Throws() throws IOException {
        var repo = repository();
        var entity1 = new KafkaTopic().setGroup("AAA").setTopic("AAA").setPartition(0);
        var entity2 = new KafkaTopic().setGroup("AAA").setTopic("BBB").setPartition(1);
        var entity3 = new KafkaTopic().setGroup("AAA").setTopic("BBB").setPartition(1);
        repo.putKafkaTopic(null, entity1);
        repo.putKafkaTopic(null, entity2);
        assertThrows(KafkaTopicAlreadyExistsException.class, () -> repo.putKafkaTopic(entity1, entity3));
    }

    @Test
    void putKafkaTopics_NonExisting_AllAdded() throws IOException {
        var repo = repository();

        var entity1 = new KafkaTopic().setGroup("AAA").setTopic("AAA").setPartition(0);
        var entity2 = new KafkaTopic().setGroup("BBB").setTopic("BBB").setPartition(0);
        repo.putKafkaTopics(List.of(entity1, entity2), true);

        var all = repo.findAllKafkaTopics();
        assertEquals(2, all.size());
    }

    @Test
    void putKafkaTopics_OneOfTwoExists_OverwriteTrue_OneAddedOtherModified() throws IOException {
        var repo = repository();

        var entity1 = new KafkaTopic().setGroup("AAA").setTopic("AAA").setPartition(0);
        var entity2 = new KafkaTopic().setGroup("BBB").setTopic("BBB").setPartition(0);
        repo.putKafkaTopics(List.of(entity1, entity2), true);

        var found = repo.findKafkaTopic("BBB", 0).orElse(null);
        assertNotNull(found);

        var entity3 = new KafkaTopic().setGroup("CCC").setTopic("CCC").setPartition(0);
        var entity4 = new KafkaTopic().setGroup("BBB").setTopic("BBB").setPartition(0);
        repo.putKafkaTopics(List.of(entity3, entity4), true);

        assertEquals(3, repo.findAllKafkaTopics().size());
        found = repo.findKafkaTopic("BBB", 0).orElse(null);
        assertNotNull(found);
        assertEquals("BBB", found.getTopic());
    }

    @Test
    void putKafkaTopics_RouteExists_OverwriteFalse_RouteNotModified_DoesNotThrow() throws IOException {
        var repo = repository();
        var entity2 = new KafkaTopic().setGroup("BBB").setTopic("BBB").setPartition(0);
        repo.putKafkaTopics(List.of(entity2), true);
        var entity4 = new KafkaTopic().setGroup("BBB").setTopic("BBB").setPartition(0);

        assertDoesNotThrow(() -> repo.putKafkaTopics(List.of(entity4), false));

        var found = repo.findKafkaTopic("BBB", 0).orElse(null);

        assertNotNull(found);
        assertEquals("BBB", found.getTopic());
    }

    @Test
    void deleteKafkaTopics_AddThenDeleteSameRoute_NoRoutesFound() throws IOException {
        var repo = repository();

        var entity1 = new KafkaTopic().setTopic("BBB");
        repo.putKafkaTopic(null, entity1);

        assertEquals(1, repo.findAllKafkaTopics().size());

        var entity2 = new KafkaTopic().setTopic("BBB");
        repo.deleteKafkaTopics(List.of(entity2));

        assertEquals(0, repo.findAllKafkaTopics().size());
    }

    @Test
    void deleteKafkaTopics_AddThenDeleteDifferentOne_OneFound() throws IOException {
        var repo = repository();

        var entity1 = new KafkaTopic().setTopic("AAA");
        repo.putKafkaTopic(null, entity1);

        assertEquals(1, repo.findAllKafkaTopics().size());

        var entity2 = new KafkaTopic().setTopic("BBB");
        repo.deleteKafkaTopics(List.of(entity2));

        assertEquals(1, repo.findAllKafkaTopics().size());
    }
}
