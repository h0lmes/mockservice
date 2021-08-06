package com.mockservice.service;

import com.mockservice.domain.Scenario;
import com.mockservice.repository.ConfigRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScenarioServiceImpl implements ScenarioService {

    private final ConfigRepository configRepository;

    public ScenarioServiceImpl(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    @Override
    public List<Scenario> getScenariosAsList() {
        return new ArrayList<>(configRepository.findAllScenarios());
    }

    @Override
    public synchronized List<Scenario> putScenario(Scenario scenario, Scenario replacement) throws IOException {
        configRepository.putScenario(scenario, replacement);
        return getScenariosAsList();
    }

    @Override
    public synchronized List<Scenario> deleteScenario(Scenario scenario) throws IOException {
        configRepository.deleteScenario(scenario);
        return getScenariosAsList();
    }
}
