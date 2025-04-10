package com.mockachu.web.webapi;

import com.mockachu.domain.Route;
import com.mockachu.model.ErrorInfo;
import com.mockachu.service.OpenApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("__webapi__/import")
@CrossOrigin(origins = "*")
public class ImportController {
    private static final Logger log = LoggerFactory.getLogger(ImportController.class);

    private final OpenApiService openApiService;

    public ImportController(OpenApiService openApiService) {
        this.openApiService = openApiService;
    }

    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Route> routesFromOpenApiYaml(@RequestBody(required = false) String yaml) throws IOException {
        return openApiService.routesFromYaml(yaml);
    }

    @ExceptionHandler(produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ErrorInfo> handleException(Exception e) {
        log.error("", e);
        return ResponseEntity.badRequest().body(ErrorInfo.of(e));
    }
}
