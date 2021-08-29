package com.mockservice.web.webapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockservice.domain.Route;
import com.mockservice.service.OpenApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("web-api/import")
@CrossOrigin(origins = "*")
public class WebApiImportController {

    private static final Logger log = LoggerFactory.getLogger(WebApiImportController.class);

    private final OpenApiService openApiService;
    private final ObjectMapper jsonMapper;

    public WebApiImportController(OpenApiService openApiService,
                                  @Qualifier("jsonMapper") ObjectMapper jsonMapper) {
        this.openApiService = openApiService;
        this.jsonMapper = jsonMapper.copy().setMixIns(getMixIns());
    }

    private Map<Class<?>, Class<?>> getMixIns() {
        Map<Class<?>, Class<?>> mixins = new HashMap<>();
        mixins.put(Route.class, Route.MixInIgnoreId.class);
        return mixins;
    }

    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public String routesFromOpenApiYaml(@RequestBody(required = false) String yaml) throws IOException {
        List<Route> routes = openApiService.routesFromYaml(yaml);
        return jsonMapper.writeValueAsString(routes);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorInfo> handleException(Exception e) {
        log.error("", e);
        return ResponseEntity
                .badRequest()
                .body(new ErrorInfo(e));
    }
}
