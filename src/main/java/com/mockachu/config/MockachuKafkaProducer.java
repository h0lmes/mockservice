package com.mockachu.config;

import org.apache.kafka.clients.consumer.ConsumerGroupMetadata;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.*;
import org.apache.kafka.common.errors.ApiException;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class MockachuKafkaProducer<K, V> implements Producer<K, V> {
    private final String clientId;
    private final Serializer<K> keySerializer;
    private final Serializer<V> valueSerializer;
    private final MockachuKafkaSender sender;

    public MockachuKafkaProducer(Map<String, Object> configs,
                                 Serializer<K> keySerializer,
                                 Serializer<V> valueSerializer,
                                 MockachuKafkaSender sender) {
        this.clientId = (String) configs.get("client.id");
        this.keySerializer = getKeySerializer(keySerializer, configs);
        this.valueSerializer = getValueSerializer(valueSerializer, configs);
        this.sender = sender;
    }

    private Serializer<K> getKeySerializer(Serializer<K> serializer,
                                                     Map<String, Object> configs) {
        if (serializer != null) return serializer;
        try {
            var clazz = (Class<?>) configs.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG);
            if (clazz != null && Serializer.class.isAssignableFrom(clazz)) {
                serializer = (Serializer) clazz.getDeclaredConstructor().newInstance();
            }
        } catch (Exception e) {
            // ignore
        }
        return serializer;
    }

    private Serializer<V> getValueSerializer(Serializer<V> serializer,
                                                     Map<String, Object> configs) {
        if (serializer != null) return serializer;
        try {
            var clazz = (Class<?>) configs.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG);
            if (clazz != null && Serializer.class.isAssignableFrom(clazz)) {
                serializer = (Serializer) clazz.getDeclaredConstructor().newInstance();
            }
        } catch (Exception e) {
            // ignore
        }
        return serializer;
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
        return this.send(producerRecord, null);
    }

    @Override
    public Future<RecordMetadata> send(ProducerRecord<K, V> producerRecord, Callback callback) {
//        ProducerRecord<K, V> interceptedRecord = this.interceptors == null ? producerRecord : this.interceptors.onSend(producerRecord);
//        return doSend(interceptedRecord, callback);
        return doSend(producerRecord, callback);
    }

    private Future<RecordMetadata> doSend(ProducerRecord<K, V> record, Callback callback) {
        try {
            long nowMs = System.currentTimeMillis();

            byte[] serializedKey;
            try {
//                if (keySerializer == null) {
//                    serializedKey = record.key().toString().getBytes(StandardCharsets.UTF_8);
//                } else {
                    serializedKey = keySerializer.serialize(record.topic(), record.headers(), record.key());
                //}
            } catch (ClassCastException cce) {
                throw new SerializationException("Can't convert key", cce);
            }

            byte[] serializedValue;
            try {
//                if (valueSerializer == null) {
//                    serializedValue = record.value().toString().getBytes(StandardCharsets.UTF_8);
//                } else {
                    serializedValue = valueSerializer.serialize(record.topic(), record.headers(), record.value());
                //}
            } catch (ClassCastException cce) {
                throw new SerializationException("Can't convert value", cce);
            }

            int partition = this.partition(record, serializedKey, serializedValue, null);

            var keyBytes = Base64.getEncoder().encode(serializedKey);
            String keyStr = new String(keyBytes, StandardCharsets.UTF_8);
            var valueBytes = Base64.getEncoder().encode(serializedValue);
            String valueStr = new String(valueBytes, StandardCharsets.UTF_8);

            return this.sender.send(
                    "",
                    record.topic() + ";" + partition + ";" + nowMs + ";" + keyStr + ";" + valueStr,
                    record.topic(),
                    partition);
        } catch (ApiException apiException) {
            var nullTopicPartition = new TopicPartition(record.topic(), 0);
            RecordMetadata nullMetadata = new RecordMetadata(nullTopicPartition, -1L, -1, -1L, -1, -1);
            if (callback != null) {
                callback.onCompletion(nullMetadata, apiException);
            }
            //this.interceptors.onSendError(record, nullTopicPartition, apiException);
            return CompletableFuture.failedFuture(apiException);
        } catch (Exception e) {
            //var nullTopicPartition = new TopicPartition(record.topic(), 0);
            //this.interceptors.onSendError(record, nullTopicPartition, e);
            throw e;
        }
    }

    private int partition(ProducerRecord<K, V> record, byte[] serializedKey, byte[] serializedValue, Cluster cluster) {
        if (record.partition() != null) {
            return record.partition();
        } else {
            return 0;
        }
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
