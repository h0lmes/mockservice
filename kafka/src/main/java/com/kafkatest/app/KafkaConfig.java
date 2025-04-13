package com.kafkatest.app;

import com.kafkatest.mockachu.MockachuKafkaConsumerFactory;
import com.kafkatest.mockachu.MockachuKafkaProducerFactory;
import com.kafkatest.mockachu.MockachuKafkaSender;
import com.kafkatest.mockachu.MockachuKafkaSenderWebClientAdapter;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConfig {
    private static final String GROUP_ID = "default";
    private static final String PRODUCER_URI = "http://localhost:8081/__kafka__/producer";
    private static final String CONSUMER_URI = "http://localhost:8081/__kafka__/consumer";

    @Bean("producerSender")
    public MockachuKafkaSender producerSender() {
        return new MockachuKafkaSenderWebClientAdapter(PRODUCER_URI, Duration.ofSeconds(5));
    }

    @Bean("consumerSender")
    public MockachuKafkaSender consumerSender() {
        return new MockachuKafkaSenderWebClientAdapter(CONSUMER_URI, Duration.ofSeconds(5));
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, CONSUMER_URI);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new MockachuKafkaConsumerFactory<>(props, consumerSender());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new MockachuKafkaProducerFactory<>(configProps, producerSender());
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
