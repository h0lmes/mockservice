package com.mockachu.kafka;

import com.mockachu.model.ErrorInfo;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("kafka/test")
@CrossOrigin(origins = "*")
public class KafkaTestController {
    private static final Logger log = LoggerFactory.getLogger(KafkaTestController.class);
    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaTestController(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> kafka() {
        kafkaTemplate.send("space", "Falcon 9");
        log.info("Sent simple message");

        List<Header> headers = new ArrayList<>();
        headers.add(new RecordHeader("trace_id", "42".getBytes()));
        ProducerRecord<String, String> rec = new ProducerRecord <>(
                "space", null, "commsat", "Iridium 66", headers);
        kafkaTemplate.send(rec);
        log.info("Sent message with headers");

        return ResponseEntity.ok("kafka messages sent");
    }

    @ExceptionHandler(produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ErrorInfo> handleException(Exception e) {
        return ResponseEntity.badRequest().body(ErrorInfo.of(e));
    }
}
