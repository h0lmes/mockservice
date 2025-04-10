package com.mockachu.web.webapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mockachu.model.ErrorInfo;
import com.mockachu.repository.ConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("__webapi__/config")
@CrossOrigin(origins = "*")
public class ConfigController {
    private static final Logger log = LoggerFactory.getLogger(ConfigController.class);

    private final ConfigRepository configRepository;

    public ConfigController(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public String getConfig() throws JsonProcessingException {
        return configRepository.getConfigData();
    }

    @PutMapping
    public void putConfig(@RequestBody String data) throws IOException {
        configRepository.writeConfigData(data);
    }

    @GetMapping("backup")
    public void backup() throws IOException {
        configRepository.backup();
    }

    @GetMapping("restore")
    public void restore() throws IOException {
        configRepository.restore();
    }

    @ExceptionHandler(produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ErrorInfo> handleException(Exception e) {
        log.error("", e);
        return ResponseEntity.badRequest().body(ErrorInfo.of(e));
    }
}
