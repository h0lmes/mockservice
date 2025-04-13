package com.kafkatest.app;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@EnableScheduling
public class TestKafkaProducer {
    private static final Logger log = LoggerFactory.getLogger(TestKafkaProducer.class);
    public static final String TOPIC = "space";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AtomicBoolean canSendSimple = new AtomicBoolean(true);
    private final AtomicBoolean canSendWithHeaders = new AtomicBoolean(true);

    public TestKafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(initialDelay = 2000, fixedRate = 3000)
    public void produce() {
        sendSimple();
        sendWithHeaders();
    }

    private void sendSimple() {
        //if (!canSendSimple.get()) return;
        //canSendSimple.set(false);

        kafkaTemplate.send(TOPIC, "Falcon 9");
//        .whenComplete((result, ex) -> {
//            if (ex != null) {
//                log.error("Couldn't send simple message:\n", ex);
//            } else {
//                log.info("Successfully sent simple message:\n{}", result);
//            }
//            canSendSimple.set(true);
//        });
        log.info("Sending simple message ...");
    }

    private void sendWithHeaders() {
        //if (!canSendWithHeaders.get()) return;
        //canSendWithHeaders.set(false);

        List<Header> headers = new ArrayList<>();
        headers.add(new RecordHeader("trace_id", UUID.randomUUID().toString().getBytes()));
        ProducerRecord<String, String> rec = new ProducerRecord <>(
                TOPIC, 0, "CommSat", "Iridium 66", headers);
        kafkaTemplate.send(rec);
//        .whenComplete((result, ex) -> {
//            if (ex != null) {
//                log.error("Couldn't send message with headers:\n", ex);
//            } else {
//                log.info("Successfully sent message with headers:\n{}", result);
//            }
//            canSendWithHeaders.set(true);
//        });
        log.info("Sending message with headers ...");
    }
}
