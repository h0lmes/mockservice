package com.mockachu.autoconfigure.kafka;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.kafka.core.KafkaTemplate;

@AutoConfiguration
@ConditionalOnClass(KafkaTemplate.class)
public class KafkaAutoConfiguration {

//    @Bean
//    @ConditionalOnMissingBean(KafkaTemplate.class)
//    public KafkaTemplate<?, ?> kafkaTemplate() {
//        return null;
//    }
}
