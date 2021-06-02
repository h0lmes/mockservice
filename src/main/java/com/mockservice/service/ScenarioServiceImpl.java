package com.mockservice.service;

import com.mockservice.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class ScenarioServiceImpl implements ScenarioService {

    private static class ActiveScenario {
        private final Scenario scenario;
        private final List<Route> routes = new ArrayList<>();

        public ActiveScenario(Scenario scenario) throws ScenarioParseException {
            this.scenario = scenario;
            parse();
        }

        private void parse() throws ScenarioParseException {
            List<String> list = scenario.getData().lines().collect(Collectors.toList());
            for (int i = 0; i < list.size(); i++) {
                try {
                    parseScenarioLine(routes, list.get(i));
                } catch (Exception e) {
                    throw new ScenarioParseException("Error while parsing scenario [" + scenario.getAlias() + "] at line " + i, e);
                }
            }
        }

        private void parseScenarioLine(List<Route> routes, String s) {
            String[] parts = s.split("\\s+");
            if (parts.length >= 2) {
                String suffix = parts.length > 2 ? parts[2] : "";
                routes.add(
                        new Route(parts[0], parts[1])
                                .setSuffix(suffix)
                );
            }
        }

        public Scenario getScenario() {
            return scenario;
        }

        public List<Route> getRoutes() {
            return routes;
        }
    }

    private final ConfigRepository configRepository;
    private final Map<String, ActiveScenario> activeScenarios = new HashMap<>();

    public ScenarioServiceImpl(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    private Config config() {
        return configRepository.getConfig();
    }

    @Override
    public List<Scenario> getScenariosAsList() {
        return new ArrayList<>(config().getScenarios());
    }

    @Override
    public synchronized List<Scenario> putScenario(Scenario scenario, Scenario replacement) throws IOException, ScenarioAlreadyExistsException {
        config().putScenario(scenario, replacement);
        configRepository.trySaveConfigToFile();
        return config().getScenarios();
    }

    @Override
    public synchronized List<Scenario> deleteScenario(Scenario scenario) throws IOException {
        boolean deleted = config().deleteScenario(scenario);
        if (deleted) {
            configRepository.trySaveConfigToFile();
        }
        return config().getScenarios();
    }

    public Set<String> getActiveScenarios() {
        return activeScenarios.keySet();
    }

    private Optional<Scenario> findByAlias(String alias) {
        return config().getScenarios().stream()
                .filter(s -> alias.equalsIgnoreCase(s.getAlias()))
                .findFirst();
    }

    @Override
    public synchronized Set<String> activateScenario(String alias) throws ScenarioParseException {
        Scenario scenario = findByAlias(alias)
                .orElseThrow(() -> new IllegalArgumentException("Scenario not found: " + alias));
        activeScenarios.put(scenario.getAlias(), new ActiveScenario(scenario));
        return getActiveScenarios();
    }

    @Override
    public synchronized Set<String> deactivateScenario(String alias) {
        Scenario scenario = findByAlias(alias)
                .orElseThrow(() -> new IllegalArgumentException("Scenario not found: " + alias));
        activeScenarios.remove(scenario.getAlias());
        return getActiveScenarios();
    }

    @Override
    public Optional<String> getRouteSuffixFromActiveScenarios(RequestMethod method, String path) {
        Predicate<Route> condition = r -> method.equals(r.getMethod()) && path.equals(r.getPath());
        return activeScenarios.values().stream()
                .map(v -> getSuffixFromActiveScenario(v, condition))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    private Optional<String> getSuffixFromActiveScenario(ActiveScenario activeScenario, Predicate<Route> condition) {
        return activeScenario.getScenario().getType().getStrategy().apply(activeScenario.getRoutes(), condition);
    }
}
