package com.mockservice.web.webapp;

import com.mockservice.domain.Scenario;
import com.mockservice.domain.ScenarioAlreadyExistsException;
import com.mockservice.domain.ScenarioParseException;
import com.mockservice.service.ActiveScenariosService;
import com.mockservice.service.ScenarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("web-api")
@CrossOrigin(origins = "*")
public class WebApiScenariosController {

    private static final Logger log = LoggerFactory.getLogger(WebApiScenariosController.class);

    private final ScenarioService scenarioService;
    private final ActiveScenariosService activeScenariosService;

    public WebApiScenariosController(ScenarioService scenarioService, ActiveScenariosService activeScenariosService) {
        this.scenarioService = scenarioService;
        this.activeScenariosService = activeScenariosService;
    }

    @GetMapping("scenarios")
    public List<Scenario> scenarios() {
        return scenarioService.getScenariosAsList();
    }

    @PutMapping("scenarios")
    public List<Scenario> putScenario(@RequestBody List<Scenario> scenarios) throws IOException, ScenarioAlreadyExistsException {
        if (scenarios.size() != 2) {
            throw new IllegalArgumentException("There must be exactly 2 scenarios, the old and the new one.");
        }
        return scenarioService.putScenario(scenarios.get(0), scenarios.get(1));
    }

    @DeleteMapping("scenarios")
    public List<Scenario> deleteScenario(@RequestBody Scenario scenario) throws IOException {
        return scenarioService.deleteScenario(scenario);
    }

    @GetMapping("scenarios/active")
    public Set<String> scenariosActive() {
        return activeScenariosService.getActiveScenarios();
    }

    @PutMapping("scenarios/active")
    public Set<String> activateScenario(@RequestBody String alias) throws ScenarioParseException {
        return activeScenariosService.activateScenario(alias);
    }

    @DeleteMapping("scenarios/active")
    public Set<String> deactivateScenario(@RequestBody String alias) {
        return activeScenariosService.deactivateScenario(alias);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorInfo> handleScenarioAlreadyExistsException(ScenarioAlreadyExistsException e) {
        log.warn(e.getMessage());
        return ResponseEntity
                .badRequest()
                .body(new ErrorInfo(e));
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorInfo> handleScenarioParseException(ScenarioParseException e) {
        log.warn(e.getMessage());
        return ResponseEntity
                .badRequest()
                .body(new ErrorInfo(e));
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorInfo> handleException(Exception e) {
        log.error("", e);
        return ResponseEntity
                .badRequest()
                .body(new ErrorInfo(e));
    }
}
