package com.mockservice.web.webapp;

import com.mockservice.domain.Route;
import com.mockservice.service.OpenApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("web-api/import")
@CrossOrigin(origins = "*")
public class WebApiImportController {

    private static final Logger log = LoggerFactory.getLogger(WebApiImportController.class);

    private final OpenApiService openApiService;

    public WebApiImportController(OpenApiService openApiService) {
        this.openApiService = openApiService;
    }

    @PutMapping("/openapi3")
    public List<Route> routesFromOpenApiYaml(@RequestBody String yaml) throws IOException {
        return openApiService.routesFromYaml(yaml);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorInfo> handleException(Exception e) {
        log.error("", e);
        return ResponseEntity
                .badRequest()
                .body(new ErrorInfo(e));
    }
}
