package com.mockachu.web.mock;

import com.mockachu.kafka.KafkaRecord;
import com.mockachu.kafka.MockachuKafkaConsumerRequest;
import com.mockachu.kafka.MockachuKafkaProducerRequest;
import com.mockachu.model.ErrorInfo;
import com.mockachu.model.KafkaTopicDto;
import com.mockachu.model.KafkaTopicRecordsDto;
import com.mockachu.service.KafkaService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("__kafka__")
@CrossOrigin(origins = "*")
public class KafkaController {
    private static final Logger log = LoggerFactory.getLogger(KafkaController.class);

    private final KafkaService kafkaService;

    public KafkaController(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    @ApiOperation(value = "Get all", tags = "kafka")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<KafkaTopicDto> list() {
        return kafkaService.list();
    }

    @ApiOperation(value = "Get all", tags = "kafka")
    @GetMapping(value = "/records", produces = MediaType.APPLICATION_JSON_VALUE)
    public KafkaTopicRecordsDto listRecords(
            @RequestParam(required = true) String topic,
            @RequestParam(required = false, defaultValue = "0") String partition,
            @RequestParam(required = false, defaultValue = "0") String offset,
            @RequestParam(required = false, defaultValue = "0") String limit) {
        return kafkaService.listRecords(topic,
                Integer.parseInt(partition),
                Long.parseLong(offset),
                Long.parseLong(limit));
    }

    @ApiOperation(value = "Create new topic or update existing one", tags = "kafka")
    @PatchMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<KafkaTopicDto> put(@RequestBody List<KafkaTopicDto> topics) throws IOException {
        kafkaService.put(topics.get(0), topics.get(1));
        return kafkaService.list();
    }

    @ApiOperation(value = "Create topics skipping existing ones", tags = "kafka")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<KafkaTopicDto> putAllSkip(@RequestBody List<KafkaTopicDto> topics) throws IOException {
        kafkaService.putAll(topics, false);
        return kafkaService.list();
    }

    @ApiOperation(value = "Create topics and update existing ones", tags = "kafka")
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<KafkaTopicDto> putAllUpdate(@RequestBody List<KafkaTopicDto> topics) throws IOException {
        kafkaService.putAll(topics, true);
        return kafkaService.list();
    }

    @ApiOperation(value = "Delete topics", tags = "kafka")
    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<KafkaTopicDto> deleteAll(@RequestBody List<KafkaTopicDto> topics) throws IOException {
        kafkaService.deleteAll(topics);
        return kafkaService.list();
    }

    @ApiOperation(value = "Put messages to a topic", tags = "kafka")
    @PostMapping(value = "/producer", produces = MediaType.APPLICATION_JSON_VALUE)
    public String producer(@RequestBody List<MockachuKafkaProducerRequest> body) {
        kafkaService.produce(body);
        return "";
    }

    @ApiOperation(value = "Consume messages from a topic", tags = "kafka")
    @PostMapping(value = "/consumer", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<List<KafkaRecord>> consumer(@RequestBody List<MockachuKafkaConsumerRequest> body) {
        return CompletableFuture.supplyAsync(() -> kafkaService.consume(body));
    }

    @ExceptionHandler(produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ErrorInfo> handleException(Exception e) {
        log.error("", e);
        return ResponseEntity.badRequest().body(ErrorInfo.of(e));
    }
}
