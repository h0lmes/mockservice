package com.mockservice.web.webapp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mockservice.domain.Route;
import com.mockservice.domain.RouteAlreadyExistsException;
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

    @GetMapping("routes")
    public List<Route> routes() {
        return configService.getRoutes().collect(Collectors.toList());
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

    @GetMapping("config")
    public String config() throws JsonProcessingException {
        return configService.getConfigData();
    }

    @PutMapping("config")
    public void putConfig(@RequestBody String data) throws IOException {
        configService.writeConfigData(data);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorInfo> handleRouteAlreadyExistsException(RouteAlreadyExistsException e) {
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
