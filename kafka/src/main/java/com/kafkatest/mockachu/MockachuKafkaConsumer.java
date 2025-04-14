package com.kafkatest.mockachu;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.consumer.internals.ConsumerUtils;
import org.apache.kafka.common.*;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.metrics.Metrics;
import org.apache.kafka.common.record.TimestampType;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.utils.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MockachuKafkaConsumer<K, V> implements Consumer<K, V> {
    private static final Logger log = LoggerFactory.getLogger(MockachuKafkaConsumer.class);

    private final String clientId;
    private final ConsumerConfig config;
    private final Deserializer<K> keyDeserializer;
    private final Deserializer<V> valueDeserializer;
    private final long retryBackoffMs;
    private final int defaultApiTimeoutMs;
    private final boolean autoCommitEnabled;
    private final Metrics metrics;
    private final AtomicBoolean wakeup = new AtomicBoolean(false);
    private final Optional<Integer> leaderEpoch = Optional.of(0);
    private final MockachuKafkaSender sender;
    private final ObjectMapper objectMapper;
    private Set<String> subscriptions;
    private Set<TopicPartition> assignments;
    private final ConsumerRecords<K,V> emptyRecords = new ConsumerRecords<>(Collections.emptyMap());

    public MockachuKafkaConsumer(ConsumerConfig config,
                                 Deserializer<K> keyDeserializer,
                                 Deserializer<V> valueDeserializer,
                                 MockachuKafkaSender sender) {
        this.clientId = config.getString("client.id");
        this.config = config;
        this.keyDeserializer = getDeserializer(keyDeserializer, config, ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG);
        this.valueDeserializer = getDeserializer(valueDeserializer, config, ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG);
        this.defaultApiTimeoutMs = config.getInt("default.api.timeout.ms");
        this.retryBackoffMs = config.getLong("retry.backoff.ms");
        this.autoCommitEnabled = config.getBoolean("enable.auto.commit");
        this.metrics = ConsumerUtils.createMetrics(config, Time.SYSTEM, List.of());

        this.sender = sender;
        this.objectMapper = new ObjectMapper();
    }

    private <T> Deserializer<T> getDeserializer(Deserializer<T> deserializer,
                                                ConsumerConfig config,
                                                String configKey) {
        if (deserializer != null) return deserializer;
        try {
            Object name = config.getString(configKey);
            if (name instanceof String className) {
                return (Deserializer<T>) Class.forName(className).getDeclaredConstructor().newInstance();
            }
        } catch (Exception e) {
            // ignore
        }
        try {
            var clazz = config.getClass(configKey);
            if (clazz != null && Deserializer.class.isAssignableFrom(clazz)) {
                return (Deserializer<T>) clazz.getDeclaredConstructor().newInstance();
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    @Override
    public Set<TopicPartition> assignment() {
        return Collections.unmodifiableSet(assignments);
    }

    @Override
    public Set<String> subscription() {
        return Collections.unmodifiableSet(subscriptions);
    }

    @Override
    public void subscribe(Collection<String> collection) {
        subscribe(collection, null);
    }

    @Override
    public void subscribe(Collection<String> collection, ConsumerRebalanceListener consumerRebalanceListener) {
        subscriptions = new HashSet<>(collection);
        updateAssignments();
    }

    @Override
    public void assign(Collection<TopicPartition> collection) {
        assignments = new HashSet<>(collection);
        log.info("Set assignments {}", assignments);
        subscriptions = assignments.stream()
                .map(TopicPartition::topic)
                .collect(Collectors.toSet());
    }

    @Override
    public void subscribe(Pattern pattern, ConsumerRebalanceListener consumerRebalanceListener) {
        throw new RuntimeException("E_NOT_IMPLEMENTED");
    }

    @Override
    public void subscribe(Pattern pattern) {
        throw new RuntimeException("E_NOT_IMPLEMENTED");
    }

    @Override
    public void unsubscribe() {
        subscriptions = Collections.emptySet();
        updateAssignments();
    }

    private void updateAssignments() {
        assignments = subscriptions.stream()
                .map(topic -> new TopicPartition(topic, 0))
                .collect(Collectors.toSet());
        log.info("Updated assignments {}", assignments);
    }

    @Override
    public ConsumerRecords<K, V> poll(long l) {
        if (wakeup.get()) {
            throw new WakeupException();
        }

        try {
            Map<TopicPartition, List<ConsumerRecord<K, V>>> map = new HashMap<>();

            query().forEach(rec -> {
                var tp = new TopicPartition(rec.topic(), rec.partition());
                var consumerRecords = map.computeIfAbsent(tp, k -> new ArrayList<>());

                Headers headers = new RecordHeaders();
                rec.headers().forEach((k, v) -> headers.add(k, v.getBytes(StandardCharsets.UTF_8)));

                int keySize = rec.key() == null ? 0 : rec.key().length();
                int valueSize = rec.value() == null ? 0 : rec.value().length();
                K key = deserialize(keyDeserializer, rec.topic(), headers, rec.key());
                V value = deserialize(valueDeserializer, rec.topic(), headers, rec.value());

                var consumerRecord = new ConsumerRecord<>(
                        rec.topic(),
                        rec.partition(),
                        rec.offset(),
                        rec.timestamp(),
                        TimestampType.CREATE_TIME,
                        keySize, valueSize, key, value,
                        headers,
                        leaderEpoch);
                consumerRecords.add(consumerRecord);
            });

            return new ConsumerRecords<>(map);
        } catch (Exception e) {
            log.error("Error: ", e);
            return emptyRecords;
        }
    }

    private List<MockachuKafkaConsumerResponse> query() throws JsonProcessingException {
        List<MockachuKafkaConsumerRequest> requests = new ArrayList<>();
        assignments.forEach(a -> requests.add(new MockachuKafkaConsumerRequest(a.topic(), a.partition(), -1L)));

        var strRequest = objectMapper.writeValueAsString(requests);
        var strResponse = sender.sendSync(strRequest);

        if (strResponse != null && !strResponse.isBlank()) {
            return objectMapper.readValue(strResponse, new TypeReference<>() {
            });
        }
        return List.of();
    }

    private <T> T deserialize(Deserializer<T> deserializer, String topic, Headers headers, String value) {
        if (value == null) return null;
        return deserializer.deserialize(topic, headers, value.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public ConsumerRecords<K, V> poll(Duration duration) {
        return poll(duration.toMillis());
    }

    @Override
    public void commitSync() {
        commitSync(Duration.ofMillis(defaultApiTimeoutMs));
    }

    @Override
    public void commitSync(Duration duration) {
        // TODO ?
    }

    @Override
    public void commitSync(Map<TopicPartition, OffsetAndMetadata> map) {
        // TODO ?
    }

    @Override
    public void commitSync(Map<TopicPartition, OffsetAndMetadata> map, Duration duration) {
        // TODO ?
    }

    @Override
    public void commitAsync() {
        commitAsync(null);
    }

    @Override
    public void commitAsync(OffsetCommitCallback offsetCommitCallback) {
        // TODO ?
    }

    @Override
    public void commitAsync(Map<TopicPartition, OffsetAndMetadata> map, OffsetCommitCallback offsetCommitCallback) {
        // do nothing
    }

    @Override
    public void seek(TopicPartition topicPartition, long l) {
        // TODO ?
    }

    @Override
    public void seek(TopicPartition topicPartition, OffsetAndMetadata offsetAndMetadata) {
        // TODO ?
    }

    @Override
    public void seekToBeginning(Collection<TopicPartition> collection) {
        // TODO ?
    }

    @Override
    public void seekToEnd(Collection<TopicPartition> collection) {
        // TODO ?
    }

    @Override
    public long position(TopicPartition topicPartition) {
        // TODO
        return 0;
    }

    @Override
    public long position(TopicPartition topicPartition, Duration duration) {
        // TODO
        return 0;
    }

    @Override
    public OffsetAndMetadata committed(TopicPartition topicPartition) {
        // TODO
        return null;
    }

    @Override
    public OffsetAndMetadata committed(TopicPartition topicPartition, Duration duration) {
        // TODO
        return null;
    }

    @Override
    public Map<TopicPartition, OffsetAndMetadata> committed(Set<TopicPartition> set) {
        Map<TopicPartition, OffsetAndMetadata> map = new HashMap<>();
        set.forEach(tp -> map.put(tp, committed(tp)));
        return map;
    }

    @Override
    public Map<TopicPartition, OffsetAndMetadata> committed(Set<TopicPartition> set, Duration duration) {
        Map<TopicPartition, OffsetAndMetadata> map = new HashMap<>();
        set.forEach(tp -> map.put(tp, committed(tp, duration)));
        return map;
    }

    @Override
    public Uuid clientInstanceId(Duration duration) {
        return Uuid.ZERO_UUID;
    }

    @Override
    public Map<MetricName, ? extends Metric> metrics() {
        return Collections.unmodifiableMap(this.metrics.metrics());
    }

    @Override
    public List<PartitionInfo> partitionsFor(String s) {
        throw new RuntimeException("E_NOT_IMPLEMENTED");
    }

    @Override
    public List<PartitionInfo> partitionsFor(String s, Duration duration) {
        throw new RuntimeException("E_NOT_IMPLEMENTED");
    }

    @Override
    public Map<String, List<PartitionInfo>> listTopics() {
        throw new RuntimeException("E_NOT_IMPLEMENTED");
    }

    @Override
    public Map<String, List<PartitionInfo>> listTopics(Duration duration) {
        throw new RuntimeException("E_NOT_IMPLEMENTED");
    }

    @Override
    public Set<TopicPartition> paused() {
        return Collections.emptySet();
    }

    @Override
    public void pause(Collection<TopicPartition> collection) {
        throw new RuntimeException("E_NOT_IMPLEMENTED");
    }

    @Override
    public void resume(Collection<TopicPartition> collection) {
        throw new RuntimeException("E_NOT_IMPLEMENTED");
    }

    @Override
    public Map<TopicPartition, OffsetAndTimestamp> offsetsForTimes(Map<TopicPartition, Long> map) {
        throw new RuntimeException("E_NOT_IMPLEMENTED");
    }

    @Override
    public Map<TopicPartition, OffsetAndTimestamp> offsetsForTimes(Map<TopicPartition, Long> map, Duration duration) {
        throw new RuntimeException("E_NOT_IMPLEMENTED");
    }

    @Override
    public Map<TopicPartition, Long> beginningOffsets(Collection<TopicPartition> collection) {
        throw new RuntimeException("E_NOT_IMPLEMENTED");
    }

    @Override
    public Map<TopicPartition, Long> beginningOffsets(Collection<TopicPartition> collection, Duration duration) {
        throw new RuntimeException("E_NOT_IMPLEMENTED");
    }

    @Override
    public Map<TopicPartition, Long> endOffsets(Collection<TopicPartition> collection) {
        throw new RuntimeException("E_NOT_IMPLEMENTED");
    }

    @Override
    public Map<TopicPartition, Long> endOffsets(Collection<TopicPartition> collection, Duration duration) {
        throw new RuntimeException("E_NOT_IMPLEMENTED");
    }

    @Override
    public OptionalLong currentLag(TopicPartition topicPartition) {
        throw new RuntimeException("E_NOT_IMPLEMENTED");
    }

    @Override
    public ConsumerGroupMetadata groupMetadata() {
        throw new RuntimeException("E_NOT_IMPLEMENTED");
    }

    @Override
    public void enforceRebalance() {
        throw new RuntimeException("E_NOT_IMPLEMENTED");
    }

    @Override
    public void enforceRebalance(String s) {
        throw new RuntimeException("E_NOT_IMPLEMENTED");
    }

    @Override
    public void close() {
        // don't care
    }

    @Override
    public void close(Duration duration) {
        // don't care
    }

    @Override
    public void wakeup() {
        wakeup.set(true);
    }
}
