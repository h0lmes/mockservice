package com.mockachu.web.webapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockachu.components.JsonFromSchemaProducer;
import com.mockachu.components.JsonProducer;
import com.mockachu.model.ErrorInfo;
import com.mockachu.util.JsonUtils;
import com.mockachu.util.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("__webapi__/generate")
@CrossOrigin(origins = "*")
public class GenerateController {
    private static final Logger log = LoggerFactory.getLogger(GenerateController.class);

    private final ObjectMapper jsonMapper;
    private final ObjectMapper yamlMapper;
    private final JsonProducer jsonProducer;
    private final JsonFromSchemaProducer jsonFromSchemaProducer;

    public GenerateController(@Qualifier("jsonMapper") ObjectMapper jsonMapper,
                              @Qualifier("yamlMapper") ObjectMapper yamlMapper,
                              JsonProducer jsonProducer,
                              JsonFromSchemaProducer jsonFromSchemaProducer) {
        this.jsonMapper = jsonMapper;
        this.yamlMapper = yamlMapper;
        this.jsonProducer = jsonProducer;
        this.jsonFromSchemaProducer = jsonFromSchemaProducer;
    }

    @GetMapping("json")
    public String json() {
        return jsonProducer.generate();
    }

    @PostMapping("json")
    public String jsonFromSchema(@RequestBody String schema) throws JsonProcessingException {
        if (JsonUtils.isJson(schema)) {
            return jsonFromSchemaProducer.jsonFromSchema(MapUtils.toMap(schema, jsonMapper));
        } else {
            return jsonFromSchemaProducer.jsonFromSchema(MapUtils.toMap(schema, yamlMapper));
        }
    }

    @ExceptionHandler(produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ErrorInfo> handleException(Exception e) {
        log.error("", e);
        return ResponseEntity.badRequest().body(ErrorInfo.of(e));
    }
}
