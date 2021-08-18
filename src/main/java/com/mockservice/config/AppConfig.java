package com.mockservice.config;

import com.mockservice.producer.*;
import com.mockservice.validate.DataValidator;
import com.mockservice.validate.JsonDataValidator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AppConfig {

    @Bean
    @Primary
    @Qualifier("jsonDataValidator")
    public DataValidator jsonDataValidator() {
        return new JsonDataValidator();
    }

    @Bean
    @Primary
    public ValueProducer valueProducer() {
        return new ValueProducerImpl();
    }

    @Bean
    @Primary
    public JsonProducer jsonProducer() {
        return new JsonProducerImpl(valueProducer());
    }

    @Bean
    @Primary
    public JsonFromSchemaProducer jsonFromSchemaProducer() {
        return new JsonFromSchemaProducerImpl(valueProducer());
    }
}
