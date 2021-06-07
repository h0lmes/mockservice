package com.mockservice.service;

import com.mockservice.domain.ScenarioParseException;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Optional;
import java.util.Set;

public interface ActiveScenariosService {
    Set<String> getActiveScenarios();
    Set<String> activateScenario(String alias) throws ScenarioParseException;
    Set<String> deactivateScenario(String alias);
    Optional<String> getAltFor(RequestMethod method, String path);
}
