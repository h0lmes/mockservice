package com.mockservice.service;

import com.mockservice.domain.Scenario;
import com.mockservice.repository.ConfigObserver;
import com.mockservice.repository.ConfigRepository;
import com.mockservice.repository.ScenarioObserver;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

@Service
public class ScenarioServiceImpl implements ScenarioService, ConfigObserver, ScenarioObserver {

    private static final Logger log = LoggerFactory.getLogger(ScenarioServiceImpl.class);

    private final ConfigRepository configRepository;
    private final Map<String, Scenario> activeScenarios = new ConcurrentHashMap<>();

    public ScenarioServiceImpl(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    @Override
    public List<Scenario> getScenariosAsList() {
        return new ArrayList<>(configRepository.findAllScenarios());
    }

    @Override
    public synchronized List<Scenario> putScenario(Scenario scenario) throws IOException {
        configRepository.putScenario(scenario);
        return getScenariosAsList();
    }

    @Override
    public synchronized List<Scenario> deleteScenario(Scenario scenario) throws IOException {
        configRepository.deleteScenario(scenario);
        return getScenariosAsList();
    }

    // --- active scenarios ----------------------------------------------------------

    @Override
    public Set<String> getActiveScenarios() {
        return new HashSet<>(activeScenarios.keySet());
    }

    @Override
    public synchronized Set<String> activateScenario(String alias) {
        Scenario scenario = findByAlias(alias)
                .orElseThrow(() -> new IllegalArgumentException("Scenario not found: " + alias));
        activateScenarioInternal(scenario);
        log.info("Scenario activated: {}", scenario);
        return getActiveScenarios();
    }

    private void activateScenarioInternal(Scenario scenario) {
        activeScenarios.put(scenario.getAlias(), scenario.setActive(true));
    }

    @Override
    public synchronized Set<String> deactivateScenario(String alias) {
        Scenario scenario = findByAlias(alias)
                .orElseThrow(() -> new IllegalArgumentException("Scenario not found: " + alias));
        deactivateScenarioInternal(scenario);
        log.info("Scenario deactivated: {}", alias);
        return getActiveScenarios();
    }

    private void deactivateScenarioInternal(Scenario scenario) {
        scenario.setActive(false);
        activeScenarios.remove(scenario.getAlias());
    }

    @Override
    public Optional<String> getAltFor(RequestMethod method, String path) {
        return activeScenarios.values().stream()
                .map(activeScenario -> activeScenario.getAltFor(method, path))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    private Optional<Scenario> findByAlias(String alias) {
        return configRepository.findAllScenarios().stream()
                .filter(s -> alias.equalsIgnoreCase(s.getAlias()))
                .findFirst();
    }

    private List<Scenario> findByAliases(Set<String> aliases) {
        return configRepository.findAllScenarios()
                .stream()
                .filter(s -> aliases.contains(s.getAlias()))
                .collect(Collectors.toList());
    }

    // --- listeners ----------------------------------------------------------

    @Override
    public void onBeforeConfigChanged() {
        // do nothing
    }

    @Override
    public void onAfterConfigChanged() {
        Set<String> active = getActiveScenarios();
        deactivateAllScenarios();
        List<Scenario> scenarios = findByAliases(active);
        scenarios.forEach(this::activateScenarioInternal);
    }

    private void deactivateAllScenarios() {
        activeScenarios.clear();
    }

    @Override
    public void onScenarioUpdated(String oldAlias, String newAlias) {
        if (scenarioIsActive(oldAlias)) {
            findByAlias(oldAlias).ifPresent(this::deactivateScenarioInternal);
            findByAlias(newAlias).ifPresent(this::activateScenarioInternal);
        }
    }

    private boolean scenarioIsActive(String alias) {
        return activeScenarios.containsKey(alias);
    }

    @Override
    public void onScenarioDeleted(String alias) {
        findByAlias(alias).ifPresent(this::deactivateScenarioInternal);
    }
}
