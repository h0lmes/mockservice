package com.mockservice.web.webapp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mockservice.domain.Route;
import com.mockservice.domain.RouteAlreadyExistsException;
import com.mockservice.domain.Scenario;
import com.mockservice.domain.ScenarioAlreadyExistsException;
import com.mockservice.service.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("web-api")
@CrossOrigin(origins = "*")
public class WebApiController {

    private static final Logger log = LoggerFactory.getLogger(WebApiController.class);

    private final ConfigService configService;

    public WebApiController(ConfigService configService) {
        this.configService = configService;
    }

    @GetMapping("config")
    public String getConfig() throws JsonProcessingException {
        return configService.getConfigData();
    }

    @PutMapping("config")
    public void putConfig(@RequestBody String data) throws IOException {
        configService.writeConfigData(data);
    }

    @GetMapping("routes")
    public List<Route> routes() {
        return configService.getRoutesAsList();
    }

    @PutMapping("routes")
    public List<Route> putRoute(@RequestBody List<Route> routes) throws IOException, RouteAlreadyExistsException {
        if (routes.size() != 2) {
            throw new IllegalArgumentException("There must be exactly 2 routes, the old and the new one.");
        }
        return configService.putRoute(routes.get(0), routes.get(1));
    }

    @DeleteMapping("routes")
    public List<Route> deleteRoute(@RequestBody Route route) throws IOException {
        return configService.deleteRoute(route);
    }

    @GetMapping("scenarios")
    public List<Scenario> scenarios() {
        return configService.getScenariosAsList();
    }

    @PutMapping("scenarios")
    public List<Scenario> putScenario(@RequestBody List<Scenario> scenarios) throws IOException, ScenarioAlreadyExistsException {
        if (scenarios.size() != 2) {
            throw new IllegalArgumentException("There must be exactly 2 scenarios, the old and the new one.");
        }
        return configService.putScenario(scenarios.get(0), scenarios.get(1));
    }

    @DeleteMapping("scenarios")
    public List<Scenario> deleteScenario(@RequestBody Scenario scenario) throws IOException {
        return configService.deleteScenario(scenario);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorInfo> handleRouteAlreadyExistsException(RouteAlreadyExistsException e) {
        log.warn(e.getMessage());
        return ResponseEntity
                .badRequest()
                .body(new ErrorInfo(e));
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorInfo> handleScenarioAlreadyExistsException(ScenarioAlreadyExistsException e) {
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
