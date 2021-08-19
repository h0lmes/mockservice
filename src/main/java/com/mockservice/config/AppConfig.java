package com.mockservice.config;

import com.mockservice.producer.*;
import com.mockservice.validate.DataValidator;
import com.mockservice.validate.JsonDataValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public DataValidator jsonDataValidator() {
        return new JsonDataValidator();
    }

    @Bean
    public ValueProducer valueProducer() {
        return new ValueProducerImpl();
    }

    @Bean
    public JsonProducer jsonProducer() {
        return new JsonProducerImpl(valueProducer());
    }

    @Bean
    public JsonFromSchemaProducer jsonFromSchemaProducer() {
        return new JsonFromSchemaProducerImpl(valueProducer());
    }
}
