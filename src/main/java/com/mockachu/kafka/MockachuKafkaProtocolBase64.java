package com.mockachu.kafka;

import org.apache.kafka.common.header.Headers;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class MockachuKafkaProtocolBase64 implements MockachuKafkaProtocol {

    @Override
    public String encodeProducerMessage(String topic, Integer partition, Long millis, byte[] key, byte[] value,
                                        Headers headers) {
        var keyBytes = Base64.getEncoder().encode(key);
        var valueBytes = Base64.getEncoder().encode(value);
        String keyStr = new String(keyBytes, StandardCharsets.UTF_8);
        String valueStr = new String(valueBytes, StandardCharsets.UTF_8);
        return "B64" + topic + ";" + partition + ";" + millis + ";" + keyStr + ";" + valueStr;
    }
}
