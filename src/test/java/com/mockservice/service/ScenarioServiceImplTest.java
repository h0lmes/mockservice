package com.mockservice.service;

import com.mockservice.domain.Scenario;
import com.mockservice.domain.ScenarioType;
import com.mockservice.repository.ConfigObserver;
import com.mockservice.repository.ConfigRepository;
import com.mockservice.repository.ScenarioObserver;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class ScenarioServiceImplTest {

    private static final String STR_1 = "line 1";
    private static final String ALIAS = "alias";
    private static final String NOT_EXISTING_ALIAS = "not-existing-alias";
    private static final RequestMethod METHOD = RequestMethod.POST;
    private static final String PATH = "/test";
    private static final String ALT1 = "400";
    private static final String ALT2 = "204";
    private static final String SCENARIO_DATA1 = METHOD.toString() + ";" + PATH + ";" + ALT1;
    private static final String SCENARIO_DATA2 = SCENARIO_DATA1 + "\n" + METHOD.toString() + ";" + PATH + ";" + ALT2;

    @Mock
    private ConfigRepository configRepository;

    private ScenarioService service() {
        return new ScenarioServiceImpl(configRepository);
    }

    @Test
    public void getScenariosAsList() {
        Scenario scenario = new Scenario().setAlias(STR_1);
        when(configRepository.findAllScenarios()).thenReturn(List.of(scenario));

        ScenarioService service = service();

        assertTrue(service.getScenariosAsList().contains(scenario));
    }

    @Test
    public void putScenario() throws IOException {
        Scenario scenario = new Scenario().setAlias(STR_1);
        when(configRepository.findAllScenarios()).thenReturn(List.of(scenario));

        ScenarioService service = service();
        List<Scenario> scenarios = service.putScenario(null, scenario);

        verify(configRepository, times(1)).putScenario(null, scenario);
        assertTrue(scenarios.contains(scenario));
    }

    @Test
    public void deleteScenario() throws IOException {
        Scenario scenario = new Scenario().setAlias(STR_1);
        when(configRepository.findAllScenarios()).thenReturn(List.of(scenario));

        ScenarioService service = service();
        service.deleteScenario(scenario);

        verify(configRepository, times(1)).deleteScenario(scenario);
    }

    // --- active scenarios ----------------------------------------------------------------

    @Test
    public void activateScenario_ScenarioNotActive_ScenarioActivates() {
        Scenario scenario = new Scenario().setAlias(ALIAS);
        when(configRepository.findAllScenarios()).thenReturn(List.of(scenario));

        ScenarioService service = service();

        assertFalse(scenario.getActive());
        service.activateScenario(ALIAS);
        assertTrue(scenario.getActive());
    }

    @Test
    public void activateScenario_ScenarioNotExists_ExceptionThrown() {
        Scenario scenario = new Scenario().setAlias(ALIAS);
        when(configRepository.findAllScenarios()).thenReturn(List.of(scenario));

        ScenarioService service = service();

        assertThrows(IllegalArgumentException.class, () -> service.activateScenario(NOT_EXISTING_ALIAS));
    }

    @Test
    public void deactivateScenario_ScenarioActive_ScenarioDeactivates() {
        Scenario scenario = new Scenario().setAlias(ALIAS);
        when(configRepository.findAllScenarios()).thenReturn(List.of(scenario));

        ScenarioService service = service();
        service.activateScenario(ALIAS);

        assertTrue(scenario.getActive());
        service.deactivateScenario(ALIAS);
        assertFalse(scenario.getActive());
    }

    @Test
    public void deactivateScenario_ScenarioNotExists_ExceptionThrown() {
        Scenario scenario = new Scenario().setAlias(ALIAS);
        when(configRepository.findAllScenarios()).thenReturn(List.of(scenario));

        ScenarioService service = service();

        assertThrows(IllegalArgumentException.class, () -> service.deactivateScenario(NOT_EXISTING_ALIAS));
    }

    @Test
    public void getAltFor_NotActiveScenario_ReturnsEmpty() {
        ScenarioService service = service();

        assertTrue(service.getAltFor(RequestMethod.POST, "/not-existing-path").isEmpty());
    }

    @Test
    public void getAltFor_ActiveScenarioMap_ReturnsSameAlt() {
        Scenario scenario = new Scenario().setAlias(ALIAS).setData(SCENARIO_DATA1);
        when(configRepository.findAllScenarios()).thenReturn(List.of(scenario));

        ScenarioService service = service();
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

        ScenarioService service = service();
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

        ScenarioService service = service();
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

    // --- listeners ----------------------------------------------------------------

    @Test
    public void onBeforeConfigChanged_DoesNothing() {
        Scenario scenario = new Scenario().setAlias(ALIAS);
        when(configRepository.findAllScenarios()).thenReturn(List.of(scenario));

        ScenarioService service = service();
        service.activateScenario(ALIAS);
        ((ConfigObserver) service).onBeforeConfigChanged();

        assertTrue(scenario.getActive());
    }

    @Test
    public void onAfterConfigChanged_ActiveScenariosExist_SameScenariosAreActive() {
        Scenario scenario = new Scenario().setAlias(ALIAS);
        when(configRepository.findAllScenarios()).thenReturn(List.of(scenario));

        ScenarioService service = service();
        service.activateScenario(ALIAS);
        ((ConfigObserver) service).onAfterConfigChanged();

        assertTrue(scenario.getActive());
    }

    @Test
    public void onScenarioCreated_NotExistingScenario_DoesNotThrow() {
        ScenarioObserver service = (ScenarioObserver) service();
        assertDoesNotThrow(() -> service.onScenarioCreated(new Scenario()));
    }
}
