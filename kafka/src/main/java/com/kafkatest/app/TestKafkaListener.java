package com.kafkatest.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class TestKafkaListener {
    private static final Logger log = LoggerFactory.getLogger(TestKafkaListener.class);

    @KafkaListener(containerFactory = "kafkaListenerContainerFactory", topics = "space")
    public void listener(
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long ts,
            @Headers MessageHeaders headers,
            @Payload String message) {

        log.info("RECEIVED ---------------------------------------------------");
        log.info("Topic/partition: {} / {}", topic, partition);

        if (log.isInfoEnabled() && headers.containsKey("trace_id")) {
            log.info("Header: trace_id = {}", new String((byte[]) headers.get("trace_id"), StandardCharsets.UTF_8));
        }

        log.info("Timestamp: {}", ts);
        log.info("Payload: {}", message);
        log.info("------------------------------------------------------------");
    }
}
