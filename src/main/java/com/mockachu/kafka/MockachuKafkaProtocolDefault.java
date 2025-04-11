package com.mockachu.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.header.Headers;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MockachuKafkaProtocolDefault implements MockachuKafkaProtocol {
    private final ObjectMapper objectMapper;

    public MockachuKafkaProtocolDefault(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String encodeProducerMessage(String topic, Integer partition,
                                        Long millis, byte[] key, byte[] value, Headers headers) {
        try {
            String sKey = key == null ? null : new String(key, StandardCharsets.UTF_8);
            String sValue = value == null ? null : new String(value, StandardCharsets.UTF_8);

            Map<String, String> map = new HashMap<>();
            headers.forEach(h -> map.put(h.key(), new String(h.value())));

            var message = new MockachuKafkaProducerMessage(topic, partition, millis, sKey, sValue, map);

            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
