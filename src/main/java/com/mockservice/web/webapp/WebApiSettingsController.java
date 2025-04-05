package com.mockservice.web.webapp;

import com.mockservice.domain.Settings;
import com.mockservice.exception.HttpServiceException;
import com.mockservice.repository.ConfigRepository;
import com.mockservice.service.HttpService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("web-api/settings")
@CrossOrigin(origins = "*")
public class WebApiSettingsController {

    private static final Logger log = LoggerFactory.getLogger(WebApiSettingsController.class);

    private final ConfigRepository configRepository;
    private final HttpService httpService;

    public WebApiSettingsController(ConfigRepository configRepository, HttpService httpService) {
        this.configRepository = configRepository;
        this.httpService = httpService;
    }

    @ApiOperation(value = "Get all settings", tags = "settings")
    @GetMapping
    public Settings getSettings() {
        return configRepository.getSettings();
    }

    @ApiOperation(value = "Save settings", tags = "settings")
    @PostMapping
    public Settings saveSettings(@RequestBody Settings settings) throws IOException {
        configRepository.setSettings(settings);
        return configRepository.getSettings();
    }

    @ApiOperation(value = "Set SSL certificate password", tags = "settings")
    @PostMapping("certificatePassword")
    public void setCertificatePassword(@RequestBody(required = false) String password) throws HttpServiceException {
        httpService.setCertificatePassword(password);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorInfo> handleException(Exception e) {
        log.error("", e);
        return ResponseEntity.badRequest().body(ErrorInfo.of(e));
    }
}
