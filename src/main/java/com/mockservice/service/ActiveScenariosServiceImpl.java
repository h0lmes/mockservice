package com.mockservice.service;

import com.mockservice.domain.Route;
import com.mockservice.domain.Scenario;
import com.mockservice.repository.ConfigChangedListener;
import com.mockservice.repository.ConfigRepository;
import com.mockservice.repository.ScenariosChangedListener;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class ActiveScenariosServiceImpl implements ActiveScenariosService, ConfigChangedListener, ScenariosChangedListener {

    private final ConfigRepository configRepository;
    private final Map<String, ActiveScenario> activeScenarios = new ConcurrentHashMap<>();

    public ActiveScenariosServiceImpl(ConfigRepository configRepository) {
        this.configRepository = configRepository;
        configRepository.registerConfigChangedListener(this);
        configRepository.registerScenariosChangedListener(this);
    }

    @Override
    public synchronized Set<String> activateScenario(String alias) {
        Scenario scenario = findByAlias(alias)
                .orElseThrow(() -> new IllegalArgumentException("Scenario not found: " + alias));
        activateScenarioInternal(scenario);
        return getActiveScenarios();
    }

    private void activateScenarioInternal(Scenario scenario) {
        activeScenarios.put(scenario.getAlias(), new ActiveScenario(scenario));
    }

    @Override
    public synchronized Set<String> deactivateScenario(String alias) {
        Scenario scenario = findByAlias(alias)
                .orElseThrow(() -> new IllegalArgumentException("Scenario not found: " + alias));
        deactivateScenarioInternal(scenario.getAlias());
        return getActiveScenarios();
    }

    private void deactivateScenarioInternal(String alias) {
        activeScenarios.remove(alias);
    }

    @Override
    public Set<String> getActiveScenarios() {
        return new HashSet<>(activeScenarios.keySet());
    }

    @Override
    public Optional<String> getAltFor(RequestMethod method, String path) {
        Predicate<Route> condition = r -> method.equals(r.getMethod()) && path.equals(r.getPath());
        return activeScenarios.values().stream()
                .map(activeScenario -> activeScenario.getScenario().getType().getStrategy()
                        .apply(activeScenario.getRoutes(), condition))
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


    // listeners ----------------------------------------------------------

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
            deactivateScenarioInternal(oldAlias);
            findByAlias(newAlias).ifPresent(this::activateScenarioInternal);
        }
    }

    private boolean scenarioIsActive(String alias) {
        return activeScenarios.containsKey(alias);
    }

    @Override
    public void onScenarioDeleted(String alias) {
        deactivateScenarioInternal(alias);
    }
}
