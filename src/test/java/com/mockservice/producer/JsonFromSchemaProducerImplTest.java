package com.mockservice.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockservice.util.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class JsonFromSchemaProducerImplTest {

    private JsonFromSchemaProducer producer() {
        return new JsonFromSchemaProducerImpl(new ValueProducerImpl());
    }

    @Test
    public void generate() throws IOException {
        String jsonSchema = IOUtils.asString("json_schema.json");
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> jsonSchemaMap = mapper.readValue(jsonSchema, Map.class);
        String json = producer().jsonFromSchema(jsonSchemaMap);
        System.out.println(json);

        assertDoesNotThrow(() -> mapper.readTree(json));
    }
}
