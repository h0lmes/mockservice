package com.mockservice.service;

import com.mockservice.domain.Scenario;
import com.mockservice.domain.ScenarioType;
import com.mockservice.repository.ConfigChangedListener;
import com.mockservice.repository.ConfigRepository;
import com.mockservice.repository.ScenariosChangedListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class ActiveScenariosServiceImplTest {

    private static final String ALIAS = "alias";
    private static final String ALIAS2 = "alias2";
    private static final String NOT_EXISTING_ALIAS = "not-existing-alias";

    private static final RequestMethod METHOD = RequestMethod.POST;
    private static final String PATH = "/test";
    private static final String ALT1 = "400";
    private static final String ALT2 = "204";
    private static final String SCENARIO_DATA1 = METHOD.toString() + ";" + PATH + ";" + ALT1;
    private static final String SCENARIO_DATA2 = SCENARIO_DATA1 + "\n" + METHOD.toString() + ";" + PATH + ";" + ALT2;

    @Mock
    private ConfigRepository configRepository;

    private ActiveScenariosService createActiveScenariosService() {
        return new ActiveScenariosServiceImpl(configRepository);
    }

    @Test
    public void activateScenario_ScenarioNotActive_ScenarioActivates() {
        Scenario scenario = new Scenario().setAlias(ALIAS);
        when(configRepository.findAllScenarios()).thenReturn(List.of(scenario));

        ActiveScenariosService service = createActiveScenariosService();

        assertTrue(service.getActiveScenarios().isEmpty());
        service.activateScenario(ALIAS);
        assertTrue(service.getActiveScenarios().contains(ALIAS));
    }

    @Test
    public void activateScenario_ScenarioNotExists_ExceptionThrown() {
        Scenario scenario = new Scenario().setAlias(ALIAS);
        when(configRepository.findAllScenarios()).thenReturn(List.of(scenario));

        ActiveScenariosService service = createActiveScenariosService();

        assertThrows(IllegalArgumentException.class, () -> service.activateScenario(NOT_EXISTING_ALIAS));
    }

    @Test
    public void deactivateScenario_ScenarioActive_ScenarioDeactivates() {
        Scenario scenario = new Scenario().setAlias(ALIAS);
        when(configRepository.findAllScenarios()).thenReturn(List.of(scenario));

        ActiveScenariosService service = createActiveScenariosService();
        service.activateScenario(ALIAS);

        assertTrue(service.getActiveScenarios().contains(ALIAS));
        service.deactivateScenario(ALIAS);
        assertTrue(service.getActiveScenarios().isEmpty());
    }

    @Test
    public void deactivateScenario_ScenarioNotExists_ExceptionThrown() {
        Scenario scenario = new Scenario().setAlias(ALIAS);
        when(configRepository.findAllScenarios()).thenReturn(List.of(scenario));

        ActiveScenariosService service = createActiveScenariosService();

        assertThrows(IllegalArgumentException.class, () -> service.deactivateScenario(NOT_EXISTING_ALIAS));
    }

    @Test
    public void getAltFor_NotActiveScenario_ReturnsEmpty() {
        ActiveScenariosService service = createActiveScenariosService();

        assertTrue(service.getAltFor(RequestMethod.POST, "/not-existing-path").isEmpty());
    }

    @Test
    public void getAltFor_ActiveScenarioMap_ReturnsSameAlt() {
        Scenario scenario = new Scenario().setAlias(ALIAS).setData(SCENARIO_DATA1);
        when(configRepository.findAllScenarios()).thenReturn(List.of(scenario));

        ActiveScenariosService service = createActiveScenariosService();
        service.activateScenario(ALIAS);

        Optional<String> alt;

        alt = service.getAltFor(METHOD, PATH);
        assertTrue(alt.isPresent());
        assertEquals(ALT1, alt.get());

        alt = service.getAltFor(METHOD, PATH);
        assertTrue(alt.isPresent());
        assertEquals(ALT1, alt.get());
    }

    @Test
    public void getAltFor_ActiveScenarioQueue_ReturnsAltThenDepletes() {
        Scenario scenario = new Scenario().setAlias(ALIAS).setData(SCENARIO_DATA1).setType(ScenarioType.QUEUE);
        when(configRepository.findAllScenarios()).thenReturn(List.of(scenario));

        ActiveScenariosService service = createActiveScenariosService();
        service.activateScenario(ALIAS);

        Optional<String> alt;

        alt = service.getAltFor(METHOD, PATH);
        assertTrue(alt.isPresent());
        assertEquals(ALT1, alt.get());

        alt = service.getAltFor(METHOD, PATH);
        assertTrue(alt.isEmpty());
    }

    @Test
    public void getAltFor_ActiveScenarioCircularQueue_ReturnsAltsInOrderAndRestartsQueue() {
        Scenario scenario = new Scenario().setAlias(ALIAS).setData(SCENARIO_DATA2).setType(ScenarioType.CIRCULAR_QUEUE);
        when(configRepository.findAllScenarios()).thenReturn(List.of(scenario));

        ActiveScenariosService service = createActiveScenariosService();
        service.activateScenario(ALIAS);

        Optional<String> alt;

        alt = service.getAltFor(METHOD, PATH);
        assertTrue(alt.isPresent());
        assertEquals(ALT1, alt.get());

        alt = service.getAltFor(METHOD, PATH);
        assertTrue(alt.isPresent());
        assertEquals(ALT2, alt.get());

        alt = service.getAltFor(METHOD, PATH);
        assertTrue(alt.isPresent());
        assertEquals(ALT1, alt.get());
    }

    // listeners ----------------------------------------------------------------

    @Test
    public void onBeforeConfigChanged_DoesNothing() {
        Scenario scenario = new Scenario().setAlias(ALIAS);
        when(configRepository.findAllScenarios()).thenReturn(List.of(scenario));

        ActiveScenariosService service = createActiveScenariosService();
        service.activateScenario(ALIAS);
        ((ConfigChangedListener) service).onBeforeConfigChanged();

        assertTrue(service.getActiveScenarios().contains(ALIAS));
    }

    @Test
    public void onAfterConfigChanged_ActiveScenariosExist_SameScenariosAreActive() {
        Scenario scenario = new Scenario().setAlias(ALIAS);
        when(configRepository.findAllScenarios()).thenReturn(List.of(scenario));

        ActiveScenariosService service = createActiveScenariosService();
        service.activateScenario(ALIAS);
        ((ConfigChangedListener) service).onAfterConfigChanged();

        assertTrue(service.getActiveScenarios().contains(ALIAS));
    }

    @Test
    public void onScenarioUpdated_NotExistingScenario_NotTrows() {
        ScenariosChangedListener service = (ScenariosChangedListener) createActiveScenariosService();

        assertDoesNotThrow(() -> service.onScenarioUpdated(NOT_EXISTING_ALIAS, ALIAS));
    }

    @Test
    public void onScenarioUpdated_ExistingActiveScenario_SameScenarioActive() {
        Scenario scenario = new Scenario().setAlias(ALIAS);
        when(configRepository.findAllScenarios()).thenReturn(List.of(scenario));

        ActiveScenariosService service = createActiveScenariosService();
        service.activateScenario(ALIAS);
        ((ScenariosChangedListener) service).onScenarioUpdated(ALIAS, ALIAS);

        assertTrue(service.getActiveScenarios().contains(ALIAS));
    }

    @Test
    public void onScenarioDeleted_ActiveScenarioExist_ScenarioDeactivated() {
        Scenario scenario1 = new Scenario().setAlias(ALIAS);
        Scenario scenario2 = new Scenario().setAlias(ALIAS2);
        when(configRepository.findAllScenarios())
                .thenReturn(List.of(scenario1))
                .thenReturn(List.of(scenario2));

        ActiveScenariosService service = createActiveScenariosService();
        service.activateScenario(ALIAS);
        service.activateScenario(ALIAS2);

        assertTrue(service.getActiveScenarios().contains(ALIAS));
        assertTrue(service.getActiveScenarios().contains(ALIAS2));

        ((ScenariosChangedListener) service).onScenarioDeleted(ALIAS);

        assertFalse(service.getActiveScenarios().contains(ALIAS));
        assertTrue(service.getActiveScenarios().contains(ALIAS2));
    }
}
