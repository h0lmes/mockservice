package com.mockachu.web.webapi;

import com.mockachu.model.ErrorInfo;
import com.mockachu.repository.ConfigRepository;
import com.mockachu.service.ContextService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("__webapi__/context")
@CrossOrigin(origins = "*")
public class ContextController {
    private static final Logger log = LoggerFactory.getLogger(ContextController.class);

    private final ContextService contextService;
    private final ConfigRepository configRepository;

    public ContextController(ContextService contextService, ConfigRepository configRepository) {
        this.contextService = contextService;
        this.configRepository = configRepository;
    }

    @ApiOperation(value = "Get global context (variables) as text", tags = "context")
    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public String get() {
        return contextService.getAsString();
    }

    @ApiOperation(value = "Set global context (variables) from provided text", tags = "context")
    @PostMapping
    public void set(@RequestBody(required = false) String body) {
        contextService.setFromString(body);
    }

    @ApiOperation(value = "Get initial context (variables) as text", tags = "context")
    @GetMapping(value = "/initial", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getInitial() {
        return configRepository.getSettings().getInitialContext();
    }

    @ApiOperation(value = "Set initial context (variables) from provided text", tags = "context")
    @PostMapping(value = "/initial")
    public void setInitial(@RequestBody(required = false) String body) throws IOException {
        configRepository.getSettings().setInitialContext(body);
        configRepository.setSettings(configRepository.getSettings());
    }

    @ExceptionHandler(produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ErrorInfo> handleException(Exception e) {
        log.error("", e);
        return ResponseEntity.badRequest().body(ErrorInfo.of(e));
    }
}
