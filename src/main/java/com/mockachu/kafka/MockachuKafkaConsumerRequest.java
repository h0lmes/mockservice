package com.mockachu.kafka;

public record MockachuKafkaConsumerRequest(String topic,
                                           Integer partition,
                                           Long seek) {
}
