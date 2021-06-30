package com.mockservice.service;

import com.mockservice.domain.Route;
import com.mockservice.domain.Scenario;
import com.mockservice.domain.ScenarioParseException;
import com.mockservice.repository.NotifiableConfigChanged;
import com.mockservice.repository.ConfigRepository;
import com.mockservice.repository.NotifiableScenariosChanged;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

@Service
public class ActiveScenariosServiceImpl implements ActiveScenariosService, NotifiableConfigChanged, NotifiableScenariosChanged {

    private static final Logger log = LoggerFactory.getLogger(ActiveScenariosServiceImpl.class);
    private final ConfigRepository configRepository;
    private final Map<String, ActiveScenario> activeScenarios = new ConcurrentHashMap<>();

    public ActiveScenariosServiceImpl(ConfigRepository configRepository) {
        this.configRepository = configRepository;
        configRepository.registerConfigChangedListener(this);
        configRepository.registerScenariosChangedListener(this);
    }

    @Override
    public void onBeforeConfigChanged() {
        // do nothing
    }

    @Override
    public void onAfterConfigChanged() {
        Set<String> active = getActiveScenarios();
        deactivateAllScenarios();
        active.forEach(this::activateScenarioSilent);
    }

    @Override
    public void onScenarioUpdated(String oldAlias, String newAlias) {
        if (scenarioIsActive(oldAlias)) {
            deactivateScenarioInternal(oldAlias);
        }
        activateScenarioSilent(newAlias);
    }

    @Override
    public void onScenarioDeleted(String alias) {
        deactivateScenarioInternal(alias);
    }

    private boolean scenarioIsActive(String alias) {
        return activeScenarios.containsKey(alias);
    }

    @Override
    public Set<String> getActiveScenarios() {
        return new HashSet<>(activeScenarios.keySet());
    }

    private void activateScenarioSilent(String alias) {
        findByAlias(alias).ifPresent(s -> {
            try {
                activateScenarioInternal(s);
            } catch (Exception e) {
                log.error("", e);
            }
        });
    }

    private Optional<Scenario> findByAlias(String alias) {
        return configRepository.findAllScenarios().stream()
                .filter(s -> alias.equalsIgnoreCase(s.getAlias()))
                .findFirst();
    }

    private void deactivateAllScenarios() {
        activeScenarios.clear();
    }

    @Override
    public synchronized Set<String> activateScenario(String alias) throws ScenarioParseException {
        Scenario scenario = findByAlias(alias)
                .orElseThrow(() -> new IllegalArgumentException("Scenario not found: " + alias));
        activateScenarioInternal(scenario);
        return getActiveScenarios();
    }

    private void activateScenarioInternal(Scenario scenario) throws ScenarioParseException {
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
    public Optional<String> getAltFor(RequestMethod method, String path) {
        Predicate<Route> condition = r -> method.equals(r.getMethod()) && path.equals(r.getPath());
        return activeScenarios.values().stream()
                .map(activeScenario -> activeScenario.getScenario().getType().getStrategy().apply(activeScenario.getRoutes(), condition))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }
}
