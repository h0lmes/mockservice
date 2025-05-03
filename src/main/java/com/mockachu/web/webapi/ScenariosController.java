package com.mockachu.web.webapi;

import com.mockachu.domain.Scenario;
import com.mockachu.exception.ScenarioAlreadyExistsException;
import com.mockachu.exception.ScenarioParseException;
import com.mockachu.model.ErrorInfo;
import com.mockachu.service.ScenarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("__webapi__/scenarios")
@CrossOrigin(origins = "*")
public class ScenariosController {
    private static final Logger log = LoggerFactory.getLogger(ScenariosController.class);

    private final ScenarioService scenarioService;

    public ScenariosController(ScenarioService scenarioService) {
        this.scenarioService = scenarioService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Scenario> scenarios() {
        return scenarioService.getScenarios();
    }

    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Scenario> putScenario(@RequestBody List<Scenario> scenarios) throws IOException {
        return scenarioService.putScenario(scenarios.get(0), scenarios.get(1));
    }

    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Scenario> deleteScenario(@RequestBody List<Scenario> scenarios) throws IOException {
        return scenarioService.deleteScenarios(scenarios);
    }

    @PutMapping(value = "/active", produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<String> activateScenario(@RequestBody String alias) {
        return scenarioService.activateScenario(alias);
    }

    @DeleteMapping(value = "/active", produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<String> deactivateScenario(@RequestBody String alias) {
        return scenarioService.deactivateScenario(alias);
    }

    @ExceptionHandler({ScenarioAlreadyExistsException.class, ScenarioParseException.class})
    protected ResponseEntity<ErrorInfo> handleScenarioException(Exception e) {
        log.warn(e.getMessage());
        return ResponseEntity.badRequest().body(ErrorInfo.of(e));
    }

    @ExceptionHandler(value = Exception.class, produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ErrorInfo> handleException(Exception e) {
        log.error("", e);
        return ResponseEntity.badRequest().body(ErrorInfo.of(e));
    }
}
