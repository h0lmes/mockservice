package com.mockservice.web.webapp;

import com.mockservice.domain.Settings;
import com.mockservice.repository.ConfigRepository;
import com.mockservice.service.MockService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("web-api/settings")
@CrossOrigin(origins = "*")
public class WebApiSettingsController {

    private static final Logger log = LoggerFactory.getLogger(WebApiSettingsController.class);

    private final ConfigRepository configRepository;
    private final MockService mockService;

    public WebApiSettingsController(ConfigRepository configRepository, MockService mockService) {
        this.configRepository = configRepository;
        this.mockService = mockService;
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

    @ApiOperation(value = "Get global variables", tags = "settings")
    @GetMapping("variables/global")
    public Map<String, String> getVariables() {
        return mockService.getGlobalVariables();
    }

    @ApiOperation(value = "Put global variables", tags = "settings")
    @PutMapping("variables/global")
    public Map<String, String> putVariables(@RequestBody Map<String, String> variables) {
        return mockService.putGlobalVariables(Optional.ofNullable(variables));
    }

    @ApiOperation(value = "Delete global variables by key", tags = "settings")
    @DeleteMapping("variables/global")
    public Map<String, String> deleteVariables(@RequestBody List<String> keys) {
        return mockService.removeGlobalVariables(Optional.ofNullable(keys));
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorInfo> handleException(Exception e) {
        log.error("", e);
        return ResponseEntity
                .badRequest()
                .body(new ErrorInfo(e));
    }
}
