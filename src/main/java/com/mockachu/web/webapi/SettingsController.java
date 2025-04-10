package com.mockachu.web.webapi;

import com.mockachu.domain.Settings;
import com.mockachu.exception.HttpServiceException;
import com.mockachu.model.ErrorInfo;
import com.mockachu.repository.ConfigRepository;
import com.mockachu.service.HttpService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("__webapi__/settings")
@CrossOrigin(origins = "*")
public class SettingsController {
    private static final Logger log = LoggerFactory.getLogger(SettingsController.class);

    private final ConfigRepository configRepository;
    private final HttpService httpService;

    public SettingsController(ConfigRepository configRepository, HttpService httpService) {
        this.configRepository = configRepository;
        this.httpService = httpService;
    }

    @ApiOperation(value = "Get all settings", tags = "settings")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Settings getSettings() {
        return configRepository.getSettings();
    }

    @ApiOperation(value = "Save settings", tags = "settings")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Settings saveSettings(@RequestBody Settings settings) throws IOException {
        configRepository.setSettings(settings);
        return configRepository.getSettings();
    }

    @ApiOperation(value = "Set SSL certificate password", tags = "settings")
    @PostMapping("certificatePassword")
    public void setCertificatePassword(@RequestBody(required = false) String password) throws HttpServiceException {
        httpService.setCertificatePassword(password);
    }

    @ExceptionHandler(produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ErrorInfo> handleException(Exception e) {
        log.error("", e);
        return ResponseEntity.badRequest().body(ErrorInfo.of(e));
    }
}
