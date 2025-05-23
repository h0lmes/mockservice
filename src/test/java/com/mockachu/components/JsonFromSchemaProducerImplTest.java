package com.mockachu.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockachu.util.IOUtils;
import com.mockachu.util.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class JsonFromSchemaProducerImplTest {

    @Mock
    private RandomUtils randomUtils;

    private JsonFromSchemaProducer producer() {
        return new JsonFromSchemaProducerImpl(new ValueProducerImpl(randomUtils), randomUtils);
    }

    @Test
    void generate() throws IOException {
        String jsonSchema = IOUtils.asString("json_schema.json");
        ObjectMapper mapper = new ObjectMapper();
        var jsonSchemaMap = (Map<String, Object>) mapper.readValue(jsonSchema, Map.class);
        String json = producer().jsonFromSchema(jsonSchemaMap);
        System.out.println(json);

        assertDoesNotThrow(() -> mapper.readTree(json));
    }
}
