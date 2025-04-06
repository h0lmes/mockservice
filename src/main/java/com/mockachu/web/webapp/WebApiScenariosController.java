package com.mockachu.web.webapp;

import com.mockachu.domain.Scenario;
import com.mockachu.exception.ScenarioAlreadyExistsException;
import com.mockachu.exception.ScenarioParseException;
import com.mockachu.service.ScenarioService;
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

    public WebApiScenariosController(ScenarioService scenarioService) {
        this.scenarioService = scenarioService;
    }

    @GetMapping("scenarios")
    public List<Scenario> scenarios() {
        return scenarioService.getScenariosAsList();
    }

    @PutMapping("scenarios")
    public List<Scenario> putScenario(@RequestBody List<Scenario> scenarios) throws IOException {
        return scenarioService.putScenario(scenarios.get(0), scenarios.get(1));
    }

    @DeleteMapping("scenarios")
    public List<Scenario> deleteScenario(@RequestBody Scenario scenario) throws IOException {
        return scenarioService.deleteScenario(scenario);
    }

    @PutMapping("scenarios/active")
    public Set<String> activateScenario(@RequestBody String alias) {
        return scenarioService.activateScenario(alias);
    }

    @DeleteMapping("scenarios/active")
    public Set<String> deactivateScenario(@RequestBody String alias) {
        return scenarioService.deactivateScenario(alias);
    }

    @ExceptionHandler({ScenarioAlreadyExistsException.class, ScenarioParseException.class})
    protected ResponseEntity<ErrorInfo> handleScenarioException(Exception e) {
        log.warn(e.getMessage());
        return ResponseEntity.badRequest().body(ErrorInfo.of(e));
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorInfo> handleException(Exception e) {
        log.error("", e);
        return ResponseEntity.badRequest().body(ErrorInfo.of(e));
    }
}
