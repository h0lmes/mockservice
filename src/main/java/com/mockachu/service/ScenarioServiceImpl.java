package com.mockachu.service;

import com.mockachu.domain.Scenario;
import com.mockachu.repository.ConfigObserver;
import com.mockachu.repository.ConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ScenarioServiceImpl implements ScenarioService, ConfigObserver {

    private static final Logger log = LoggerFactory.getLogger(ScenarioServiceImpl.class);

    private final ConfigRepository configRepository;
    private Set<String> active;

    public ScenarioServiceImpl(ConfigRepository configRepository) {
        this.configRepository = configRepository;
        this.configRepository.registerConfigObserver(this);
    }

    @Override
    public List<Scenario> getScenarios() {
        return new ArrayList<>(configRepository.findAllScenarios());
    }

    @Override
    public synchronized List<Scenario> putScenario(@Nullable Scenario originalScenario, @Nonnull Scenario scenario) throws IOException {
        configRepository.putScenario(originalScenario, scenario);
        return getScenarios();
    }

    @Override
    public synchronized List<Scenario> deleteScenario(Scenario scenario) throws IOException {
        configRepository.deleteScenario(scenario);
        return getScenarios();
    }

    // --- active scenarios ----------------------------------------------------------

    @Override
    public Set<String> getActiveScenarios() {
        return getActiveScenariosStream()
                .map(Scenario::getAlias)
                .collect(Collectors.toSet());
    }

    private Stream<Scenario> getActiveScenariosStream() {
        return configRepository.findAllScenarios().stream()
                .filter(Scenario::getActive);
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
        scenario.setActive(true);
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
    }

    @Override
    public Optional<String> getAltFor(RequestMethod method, String path) {
        return getActiveScenariosStream()
                .map(s -> s.getAltFor(method, path))
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
        if (aliases == null) {
            return List.of();
        }
        return configRepository.findAllScenarios()
                .stream()
                .filter(s -> aliases.contains(s.getAlias()))
                .toList();
    }

    // --- listeners ----------------------------------------------------------

    @Override
    public void onBeforeConfigChanged() {
        active = getActiveScenarios();
    }

    @Override
    public void onAfterConfigChanged() {
        List<Scenario> scenarios = findByAliases(active);
        scenarios.forEach(this::activateScenarioInternal);
    }
}
