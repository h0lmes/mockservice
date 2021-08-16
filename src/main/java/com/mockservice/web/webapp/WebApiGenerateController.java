package com.mockservice.web.webapp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockservice.util.JsonFromSchemaGenerator;
import com.mockservice.util.JsonGenerator;
import com.mockservice.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("web-api/generate")
@CrossOrigin(origins = "*")
public class WebApiGenerateController {

    private static final Logger log = LoggerFactory.getLogger(WebApiGenerateController.class);

    private final ObjectMapper jsonMapper;
    private final ObjectMapper yamlMapper;

    public WebApiGenerateController(@Qualifier("jsonMapper") ObjectMapper jsonMapper,
                                    @Qualifier("yamlMapper") ObjectMapper yamlMapper) {
        this.jsonMapper = jsonMapper;
        this.yamlMapper = yamlMapper;
    }

    @GetMapping("json")
    public String json() {
        return JsonGenerator.generate();
    }

    @PostMapping("json")
    @SuppressWarnings("unchecked")
    public String jsonFromSchema(@RequestBody String schema) throws JsonProcessingException {
        if (JsonUtils.isJson(schema)) {
            Map<String, Object> jsonSchemaMap = jsonMapper.readValue(schema, Map.class);
            return JsonFromSchemaGenerator.jsonFromSchema(jsonSchemaMap);
        } else {
            Map<String, Object> jsonSchemaMap = yamlMapper.readValue(schema, Map.class);
            return JsonFromSchemaGenerator.jsonFromSchema(jsonSchemaMap);
        }
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorInfo> handleException(Exception e) {
        log.error("", e);
        return ResponseEntity
                .badRequest()
                .body(new ErrorInfo(e));
    }
}
