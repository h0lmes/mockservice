package com.mockservice.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import org.springframework.stereotype.Service;

@Service
public class YamlMapperService {

    private final ObjectMapper yamlMapper;
    private final ObjectMapper jsonMapper;

    public YamlMapperService() {
        this.yamlMapper = getYamlObjectMapper();
        this.jsonMapper = getJsonObjectMapper();
    }

    private ObjectMapper getYamlObjectMapper() {
        YAMLFactory factory = new YAMLFactory();
        factory.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);
        factory.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES);
        factory.enable(YAMLGenerator.Feature.LITERAL_BLOCK_STYLE);
        factory.disable(YAMLGenerator.Feature.INDENT_ARRAYS);
        factory.disable(YAMLGenerator.Feature.SPLIT_LINES);
        ObjectMapper mapper = new ObjectMapper(factory);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.findAndRegisterModules();
        return mapper;
    }

    public ObjectReader yamlReader() {
        return yamlMapper.reader();
    }

    public ObjectWriter yamlWriter() {
        return yamlMapper.writer();
    }

    private ObjectMapper getJsonObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.findAndRegisterModules();
        return mapper;
    }

    public ObjectReader jsonReader() {
        return jsonMapper.reader();
    }

    public ObjectWriter jsonWriter() {
        return jsonMapper.writer();
    }
}
