package com.mockservice.web.webapp;

import com.mockservice.domain.Settings;
import com.mockservice.repository.ConfigRepository;
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

    public WebApiSettingsController(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    @ApiOperation(value = "Return Settings", tags = "settings")
    @GetMapping
    public Settings getSettings() {
        return configRepository.getSettings();
    }

    @ApiOperation(value = "Store Settings", tags = "settings")
    @PutMapping
    public Settings putSettings(@RequestBody Settings settings) throws IOException {
        configRepository.setSettings(settings);
        return configRepository.getSettings();
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorInfo> handleException(Exception e) {
        log.error("", e);
        return ResponseEntity
                .badRequest()
                .body(new ErrorInfo(e));
    }
}
