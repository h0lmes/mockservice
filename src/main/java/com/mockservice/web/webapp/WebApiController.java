package com.mockservice.web.webapp;

import com.mockservice.mockconfig.Config;
import com.mockservice.mockconfig.Route;
import com.mockservice.service.ConfigService;
import com.mockservice.service.ResourceService;
import com.mockservice.service.model.RestErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("web-api")
@CrossOrigin(origins = "*")
public class WebApiController {

    private static final Logger log = LoggerFactory.getLogger(WebApiController.class);

    private final ResourceService resourceService;
    private final ConfigService configService;

    public WebApiController(ResourceService resourceService, ConfigService configService) {
        this.resourceService = resourceService;
        this.configService = configService;
    }

    @GetMapping("datafiles")
    public List<Route> dataFiles() {
        return resourceService.files();
    }

    @GetMapping("config")
    public Config config() {
        return configService.getConfig();
    }

    @PutMapping("route")
    public Config putRoute(@RequestBody Route route) throws IOException {
        return configService.putRoute(route);
    }

    @DeleteMapping("route")
    public Config deleteRoute(@RequestBody Route route) throws IOException {
        return configService.deleteRoute(route);
    }

    @ExceptionHandler
    protected ResponseEntity<RestErrorResponse> handleException(Throwable t) {
        log.error("", t);
        return ResponseEntity
                .badRequest()
                .body(new RestErrorResponse(t));
    }
}
