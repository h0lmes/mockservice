package com.mockachu.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockachu.kafka.MockachuKafkaConsumerRequest;
import com.mockachu.kafka.MockachuKafkaProducerRequest;
import com.mockachu.mapper.KafkaTopicMapperImpl;
import com.mockachu.repository.ConfigRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class KafkaServiceImplTest {

    @Mock
    private ConfigRepository repository;

    private KafkaService service() {
        return new KafkaServiceImpl(repository, new KafkaTopicMapperImpl(), new ObjectMapper());
    }

    @Test
    void WHEN_consumeEmptyTopic_THEN_noRecordsFound() {
        var consumerRequest = new MockachuKafkaConsumerRequest("A", 0, -1L);
        var service = service();

        var records = service.consume(List.of(consumerRequest));
        assertNotNull(records);
        assertEquals(0, records.size());
    }

    @Test
    void WHEN_consumeWithNullRequest_THEN_noRecordsFound() {
        var service = service();

        var records = service.consume(null);
        assertNotNull(records);
        assertEquals(0, records.size());
    }

    @Test
    void WHEN_consumeWithEmptyRequest_THEN_noRecordsFound() {
        var service = service();

        var records = service.consume(List.of());
        assertNotNull(records);
        assertEquals(0, records.size());
    }

    @Test
    void WHEN_produceAndConsume_THEN_recordsFoundInOrderTheyWereProduced() {
        var producerRequest1 = new MockachuKafkaProducerRequest("A", 0, 1L, "B", "C", null);
        var producerRequest2 = new MockachuKafkaProducerRequest("A", 0, 1L, "D", "E", null);
        var consumerRequest = new MockachuKafkaConsumerRequest("A", 0, -1L);
        var service = service();
        service.produce(List.of(producerRequest1, producerRequest2));

        var records = service.consume(List.of(consumerRequest));
        assertNotNull(records);
        assertEquals(2, records.size());
        assertEquals("B", records.get(0).key());
        assertEquals("C", records.get(0).value());
        assertEquals("D", records.get(1).key());
        assertEquals("E", records.get(1).value());
    }

    @Test
    void WHEN_produceAndConsumeDifferentTopic_THEN_noRecordsFound() {
        var producerRequest1 = new MockachuKafkaProducerRequest("A", 0, 1L, "B", "C", null);
        var producerRequest2 = new MockachuKafkaProducerRequest("A", 0, 1L, "D", "E", null);
        var consumerRequest = new MockachuKafkaConsumerRequest("B", 0, -1L);
        var service = service();
        service.produce(List.of(producerRequest1, producerRequest2));

        var records = service.consume(List.of(consumerRequest));
        assertNotNull(records);
        assertTrue(records.isEmpty());
    }

    @Test
    void WHEN_produceAndList_THEN_topicFound() {
        var producerRequest1 = new MockachuKafkaProducerRequest("A", 0, 1L, "B", "C", null);
        var producerRequest2 = new MockachuKafkaProducerRequest("A", 0, 1L, "D", "E", null);
        var service = service();
        service.produce(List.of(producerRequest1, producerRequest2));

        var list = service.list();
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("A", list.get(0).getTopic());
        assertEquals(0, list.get(0).getPartition());
    }
}
