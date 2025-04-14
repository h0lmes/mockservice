package com.mockachu.web.mock;

import com.mockachu.kafka.KafkaRecord;
import com.mockachu.kafka.MockachuKafkaConsumerRequest;
import com.mockachu.kafka.MockachuKafkaProducerRequest;
import com.mockachu.model.ErrorInfo;
import com.mockachu.service.KafkaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("__kafka__")
@CrossOrigin(origins = "*")
public class KafkaController {
    private static final Logger log = LoggerFactory.getLogger(KafkaController.class);

    private final KafkaService kafkaService;

    public KafkaController(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    @PostMapping(value = "/producer", produces = MediaType.APPLICATION_JSON_VALUE)
    public String producer(@RequestBody List<MockachuKafkaProducerRequest> body) {
        kafkaService.produce(body);
        return "";
    }

    @PostMapping(value = "/consumer", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<KafkaRecord> consumer(@RequestBody List<MockachuKafkaConsumerRequest> body) {
        return kafkaService.consume(body);
    }

    @ExceptionHandler(produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ErrorInfo> handleException(Exception e) {
        log.error("", e);
        return ResponseEntity.badRequest().body(ErrorInfo.of(e));
    }
}
