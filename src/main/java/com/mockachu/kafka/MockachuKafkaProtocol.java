package com.mockachu.kafka;

import org.apache.kafka.common.header.Headers;

public interface MockachuKafkaProtocol {
    String encodeProducerMessage(String topic, Integer partition, Long millis, byte[] key, byte[] value, Headers headers);
}
