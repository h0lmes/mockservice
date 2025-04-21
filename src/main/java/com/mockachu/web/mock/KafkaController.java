package com.mockachu.web.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("__kafka__")
@CrossOrigin(origins = "*")
public class KafkaController {
    private static final Logger log = LoggerFactory.getLogger(KafkaController.class);

    private final KafkaService kafkaService;
    private final ObjectMapper objectMapper;

    public KafkaController(KafkaService kafkaService,
                           @Qualifier("jsonMapper") ObjectMapper objectMapper) {
        this.kafkaService = kafkaService;
        this.objectMapper = objectMapper;
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

    @ApiOperation(value = "Inbound message format example", tags = "kafka")
    @PostMapping(value = "/append-item", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<MockachuKafkaProducerRequest> appendItem(
            @RequestBody(required = false) String appendTo,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String partition) throws JsonProcessingException {
        List<MockachuKafkaProducerRequest> list = readList(appendTo);

        if (topic == null || topic.isBlank()) topic = "topic";

        if (partition == null || partition.isBlank()) partition = "0";
        int partitionValue = Integer.parseInt(partition);

        list.add(new MockachuKafkaProducerRequest(
                topic, partitionValue, null, "key", "value", Map.of("key", "value")));

        return list;
    }

    private List<MockachuKafkaProducerRequest> readList(String value) throws JsonProcessingException {
        if (value == null || value.isBlank()) return new ArrayList<>();
        return objectMapper.readValue(value, new TypeReference<>() {});
    }

    @ExceptionHandler(produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ErrorInfo> handleException(Exception e) {
        log.error("", e);
        return ResponseEntity.badRequest().body(ErrorInfo.of(e));
    }
}
