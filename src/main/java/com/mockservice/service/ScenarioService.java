package com.mockservice.service;

import com.mockservice.domain.Scenario;
import com.mockservice.domain.ScenarioAlreadyExistsException;

import java.io.IOException;
import java.util.List;

public interface ScenarioService {
    List<Scenario> getScenariosAsList();
    List<Scenario> putScenario(Scenario scenario, Scenario replacement) throws IOException, ScenarioAlreadyExistsException;
    List<Scenario> deleteScenario(Scenario scenario) throws IOException;
}
