package com.mockachu.service;

import com.mockachu.domain.Scenario;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ScenarioService {
    List<Scenario> getScenariosAsList();
    List<Scenario> putScenario(@Nullable Scenario originalScenario, @Nonnull Scenario scenario) throws IOException;
    List<Scenario> deleteScenario(Scenario scenario) throws IOException;

    Set<String> getActiveScenarios();
    Set<String> activateScenario(String alias);
    Set<String> deactivateScenario(String alias);
    Optional<String> getAltFor(RequestMethod method, String path);
}
