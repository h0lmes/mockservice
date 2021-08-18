package com.mockservice.service;

import com.mockservice.domain.Scenario;
import com.mockservice.repository.ConfigRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class ScenarioServiceImplTest {

    private static final String STR_1 = "line 1";

    @Mock
    private ConfigRepository configRepository;

    private ScenarioService createScenarioService() {
        return new ScenarioServiceImpl(configRepository);
    }

    @Test
    public void getScenariosAsList() {
        Scenario scenario = new Scenario().setAlias(STR_1);
        when(configRepository.findAllScenarios()).thenReturn(List.of(scenario));

        ScenarioService service = createScenarioService();

        assertTrue(service.getScenariosAsList().contains(scenario));
        assertThrows(UnsupportedOperationException.class, () -> service.getScenariosAsList().add(scenario));
    }
}
