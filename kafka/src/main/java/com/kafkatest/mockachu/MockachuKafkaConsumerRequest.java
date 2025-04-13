package com.kafkatest.mockachu;

public record MockachuKafkaConsumerRequest(String topic,
                                           Integer partition,
                                           Long seek) {
}
