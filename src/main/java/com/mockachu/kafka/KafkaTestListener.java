package com.mockachu.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class KafkaTestListener {
    private static final Logger log = LoggerFactory.getLogger(KafkaTestListener.class);

    @KafkaListener(containerFactory = "kafkaListenerContainerFactory", topics = "space")
    public void listener(
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long ts,
            @Headers MessageHeaders headers,
            @Payload String message) {
        log.info("HEADERS -> {}", headers.keySet());
        log.info("TOPIC -> {}", topic);
        log.info("TIMESTAMP -> {}", ts);
        log.info("MESSAGE -> {}", message);
    }
}
