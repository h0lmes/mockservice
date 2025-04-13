package com.kafkatest.mockachu;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerGroupMetadata;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.*;
import org.apache.kafka.common.errors.ApiException;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class MockachuKafkaProducer<K, V> implements Producer<K, V> {
    private static final Logger log = LoggerFactory.getLogger(MockachuKafkaProducer.class);

    private final String clientId;
    private final Serializer<K> keySerializer;
    private final Serializer<V> valueSerializer;
    private final MockachuKafkaSender sender;
    private final ObjectMapper objectMapper;

    public MockachuKafkaProducer(Map<String, Object> configs,
                                 Serializer<K> keySerializer,
                                 Serializer<V> valueSerializer,
                                 MockachuKafkaSender sender) {
        this.clientId = (String) configs.get("client.id");
        this.keySerializer = getSerializer(keySerializer, configs, ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG);
        this.valueSerializer = getSerializer(valueSerializer, configs, ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG);
        this.sender = sender;
        this.objectMapper = new ObjectMapper();
    }

    private <T> Serializer<T> getSerializer(Serializer<T> serializer,
                                            Map<String, Object> configs,
                                            String configKey) {
        if (serializer != null) return serializer;
        try {
            Object name = configs.get(configKey);
            if (name instanceof String className) {
                return (Serializer<T>) Class.forName(className).getDeclaredConstructor().newInstance();
            }
        } catch (Exception e) {
            // ignore
        }
        try {
            var clazz = (Class<T>) configs.get(configKey);
            if (clazz != null && Serializer.class.isAssignableFrom(clazz)) {
                return (Serializer<T>) clazz.getDeclaredConstructor().newInstance();
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    @Override
    public void initTransactions() {
        // don't care
    }

    @Override
    public void beginTransaction() throws ProducerFencedException {
        // don't care
    }

    @Override
    public void sendOffsetsToTransaction(Map<TopicPartition, OffsetAndMetadata> map, String s) throws ProducerFencedException {
        // don't care
    }

    @Override
    public void sendOffsetsToTransaction(Map<TopicPartition, OffsetAndMetadata> map, ConsumerGroupMetadata consumerGroupMetadata) throws ProducerFencedException {
        // don't care
    }

    @Override
    public void commitTransaction() throws ProducerFencedException {
        // don't care
    }

    @Override
    public void abortTransaction() throws ProducerFencedException {
        // don't care
    }

    @Override
    public Future<RecordMetadata> send(ProducerRecord<K, V> producerRecord) {
        return send(producerRecord, null);
    }

    @Override
    public Future<RecordMetadata> send(ProducerRecord<K, V> producerRecord, Callback callback) {
//        ProducerRecord<K, V> interceptedRecord = this.interceptors == null ? record : this.interceptors.onSend(record);
//        return doSend(interceptedRecord, callback);
        return doSend(producerRecord, callback);
    }

    private Future<RecordMetadata> doSend(ProducerRecord<K, V> producerRecord, Callback callback) {
        try {
            long millis = producerRecord.timestamp() != null ? producerRecord.timestamp() : System.currentTimeMillis();

            byte[] serializedKey;
            try {
                serializedKey = keySerializer.serialize(
                        producerRecord.topic(), producerRecord.headers(), producerRecord.key());
            } catch (ClassCastException cce) {
                throw new SerializationException("Can't convert key", cce);
            }

            byte[] serializedValue;
            try {
                serializedValue = valueSerializer.serialize(
                        producerRecord.topic(), producerRecord.headers(), producerRecord.value());
            } catch (ClassCastException cce) {
                throw new SerializationException("Can't convert value", cce);
            }
            var serializedKeyLen = serializedKey == null ? 0 : serializedKey.length;
            var serializedValueLen = serializedValue == null ? 0 : serializedValue.length;

            int partition = partition(producerRecord, serializedKey, serializedValue, null);
            var topicPartition = new TopicPartition(producerRecord.topic(), partition);

            var message = encodeMessage(producerRecord.topic(), partition,
                    millis, serializedKey, serializedValue, producerRecord.headers());

            var strMessage = objectMapper.writeValueAsString(List.of(message));

            var future = new CompletableFuture<RecordMetadata>();
            var senderFuture = sender.sendAsync(strMessage);
            senderFuture.whenComplete((result, error) -> {
                if (error != null) {
                    log.error(error.getMessage());
                    future.completeExceptionally(error);
                    return;
                }

                var offset = 0; // TODO receive offset
                var metadata = new RecordMetadata(
                        topicPartition, offset, 0, millis, serializedKeyLen, serializedValueLen);
                future.complete(metadata);
            });
            return future;
        } catch (ApiException apiException) {
            var nullTopicPartition = new TopicPartition(producerRecord.topic(), 0);
            RecordMetadata nullMetadata = new RecordMetadata(
                    nullTopicPartition, -1L, -1, -1L, -1, -1);
            if (callback != null) {
                callback.onCompletion(nullMetadata, apiException);
            }
            //this.interceptors.onSendError(record, nullTopicPartition, apiException);
            return CompletableFuture.failedFuture(apiException);
        } catch (Exception e) {
            //var nullTopicPartition = new TopicPartition(record.topic(), 0);
            //this.interceptors.onSendError(record, nullTopicPartition, e);
            throw new RuntimeException(e);
        }
    }

    private MockachuKafkaProducerRequest encodeMessage(
            String topic, Integer partition, Long millis, byte[] key, byte[] value, Headers headers) {
        String sKey = key == null ? null : new String(key, StandardCharsets.UTF_8);
        String sValue = value == null ? null : new String(value, StandardCharsets.UTF_8);

        Map<String, String> map = new HashMap<>();
        headers.forEach(h -> map.put(h.key(), new String(h.value())));

        return new MockachuKafkaProducerRequest(topic, partition, millis, sKey, sValue, map);
    }

    private int partition(ProducerRecord<K, V> producerRecord, byte[] serializedKey, byte[] serializedValue, Cluster cluster) {
        return producerRecord.partition() != null ? producerRecord.partition() : 0;
    }

    @Override
    public void flush() {
        // don't care
    }

    @Override
    public List<PartitionInfo> partitionsFor(String s) {
        return Collections.emptyList();
    }

    @Override
    public Map<MetricName, ? extends Metric> metrics() {
        return Collections.emptyMap();
    }

    @Override
    public Uuid clientInstanceId(Duration duration) {
        return null;
    }

    @Override
    public void close() {
        // don't care
    }

    @Override
    public void close(Duration duration) {
        // don't care
    }
}
