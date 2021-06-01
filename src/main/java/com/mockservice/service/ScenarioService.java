package com.mockservice.service;

import com.mockservice.domain.Scenario;
import com.mockservice.domain.ScenarioAlreadyExistsException;
import com.mockservice.domain.ScenarioParseException;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ScenarioService {
    List<Scenario> getScenariosAsList();
    List<Scenario> putScenario(Scenario scenario, Scenario replacement) throws IOException, ScenarioAlreadyExistsException;
    List<Scenario> deleteScenario(Scenario scenario) throws IOException;
    Set<String> getActiveScenarios();
    Set<String> activateScenario(String alias) throws ScenarioParseException;
    Set<String> deactivateScenario(String alias);
    Optional<String> getRouteSuffixFromScenario(RequestMethod method, String path);
}
