package com.mockachu.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockachu.domain.KafkaTopic;
import com.mockachu.kafka.MockachuKafkaConsumerRequest;
import com.mockachu.kafka.MockachuKafkaProducerRequest;
import com.mockachu.mapper.KafkaTopicMapperImpl;
import com.mockachu.model.KafkaTopicDto;
import com.mockachu.repository.ConfigRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaServiceImplTest {

    @Mock
    private ConfigRepository configRepository;

    private KafkaService service() {
        return new KafkaServiceImpl(configRepository, new KafkaTopicMapperImpl(), new ObjectMapper());
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

    @Test
    void WHEN_noTopicsAndListRecords_THEN_noRecordsFound() {
        var service = service();

        var dto = service.listRecords("A", 0, 0L, 10L);
        assertNotNull(dto);
        assertNotNull(dto.getRecords());
        assertEquals(10L, dto.getLimit());
        assertEquals(0L, dto.getOffset());
        assertEquals(0, dto.getRecords().size());
    }

    @Test
    void WHEN_produceAndListRecords_THEN_recordsFoundInOrder() {
        var producerRequest1 = new MockachuKafkaProducerRequest(
                "A", 42, 1L, "B", "C", null);
        var producerRequest2 = new MockachuKafkaProducerRequest(
                "A", 42, 1L, "D", "E", null);
        var service = service();
        service.produce(List.of(producerRequest1, producerRequest2));

        var dto = service.listRecords("A", 42, 0L, 10L);
        assertNotNull(dto);
        assertNotNull(dto.getRecords());
        assertEquals(10L, dto.getLimit());
        assertEquals(0L, dto.getOffset());
        assertEquals(2, dto.getRecords().size());
        assertEquals("B", dto.getRecords().get(0).key());
        assertEquals("C", dto.getRecords().get(0).value());
        assertEquals("D", dto.getRecords().get(1).key());
        assertEquals("E", dto.getRecords().get(1).value());
    }

    @Test
    void WHEN_listRecordsForTopicWithInitialData_THEN_recordsFoundInOrder() {
        var entity = new KafkaTopic().setTopic("A").setPartition(42)
                .setInitialData("""
                        [{
                            "topic": "A",
                            "partition": 42,
                            "key": "B",
                            "value": "C"
                        }]
                        """);
        when(configRepository.findAllKafkaTopics()).thenReturn(List.of(entity));
        var service = service();

        var dto = service.listRecords("A", 42, 0L, 10L);
        assertNotNull(dto);
        assertNotNull(dto.getRecords());
        assertEquals(10L, dto.getLimit());
        assertEquals(0L, dto.getOffset());
        assertEquals(1, dto.getRecords().size());
        assertEquals("B", dto.getRecords().get(0).key());
        assertEquals("C", dto.getRecords().get(0).value());
    }

    //---------------------------------------------------------------------
    //
    // repo operations
    //
    //---------------------------------------------------------------------

    @Test
    void WHEN_listAndTopicInRepo_THEN_topicFound() {
        var entity = new KafkaTopic().setTopic("topic").setPartition(42);
        when(configRepository.findAllKafkaTopics()).thenReturn(List.of(entity));
        var service = service();
        var list = service.list();

        assertEquals(1, list.size());
        assertEquals("topic", list.get(0).getTopic());
        assertEquals(42, list.get(0).getPartition());
    }

    @Test
    void WHEN_put_THEN_callsRepository() throws IOException {
        var service = service();
        service.put(new KafkaTopicDto(), new KafkaTopicDto());

        verify(configRepository, times(1))
                .putKafkaTopic(any(), any());
    }

    @Test
    void WHEN_putAll_THEN_callsRepository() throws IOException {
        var service = service();
        service.putAll(List.of(new KafkaTopicDto()), true);

        verify(configRepository, times(1))
                .putKafkaTopics(anyList(), anyBoolean());
    }

    @SuppressWarnings("unchecked")
    @Test
    void WHEN_deleteAll_THEN_callsRepository() throws IOException {
        var service = service();
        service.deleteAll(List.of(new KafkaTopicDto().setTopic("topic")));

        ArgumentCaptor<List<KafkaTopic>> captor = ArgumentCaptor.forClass(List.class);
        verify(configRepository).deleteKafkaTopics(captor.capture());
        assertFalse(captor.getValue().isEmpty());
        assertEquals("topic", captor.getValue().get(0).getTopic());
    }
}
