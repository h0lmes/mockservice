package com.mockservice.web.webapp;

import com.mockservice.domain.Route;
import com.mockservice.service.OpenApiService;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("web-api/import")
@CrossOrigin(origins = "*")
public class WebApiImportController {

    private static final Logger log = LoggerFactory.getLogger(WebApiImportController.class);

    private final OpenApiService openApiService;

    public WebApiImportController(OpenApiService openApiService) {
        this.openApiService = openApiService;
    }

    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Route> routesFromOpenApiYaml(@RequestBody(required = false) String yaml) throws IOException {
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
