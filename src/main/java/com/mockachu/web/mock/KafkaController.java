package com.mockachu.web.mock;

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
@RequestMapping("__kafka__")
@CrossOrigin(origins = "*")
public class KafkaController {
    private static final Logger log = LoggerFactory.getLogger(KafkaController.class);
    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaController(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @GetMapping(value = "/test", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> kafka() {
        kafkaTemplate.send("space", "Falcon 9");

        List<Header> headers = new ArrayList<>();
        headers.add(new RecordHeader("trace_id", "42".getBytes()));
        ProducerRecord<String, String> record = new ProducerRecord <>(
                "LEO", null, "commsat", "Iridium 66", headers);
        kafkaTemplate.send(record);

        return ResponseEntity.ok("kafka messages sent");
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public String put(@RequestBody(required = false) String body) {
        log.info(body);
        return "offset=0";
    }

    @ExceptionHandler(produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ErrorInfo> handleException(Exception e) {
        return ResponseEntity.badRequest().body(ErrorInfo.of(e));
    }
}
