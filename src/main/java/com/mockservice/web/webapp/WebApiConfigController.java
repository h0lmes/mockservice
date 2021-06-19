package com.mockservice.web.webapp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mockservice.service.ConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("web-api/config")
@CrossOrigin(origins = "*")
public class WebApiConfigController {

    private static final Logger log = LoggerFactory.getLogger(WebApiConfigController.class);

    private final ConfigRepository configRepository;

    public WebApiConfigController(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    @GetMapping
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

    @ExceptionHandler
    protected ResponseEntity<ErrorInfo> handleException(Exception e) {
        log.error("", e);
        return ResponseEntity
                .badRequest()
                .body(new ErrorInfo(e));
    }
}
