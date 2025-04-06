package com.mockachu.config;

import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.mockachu.components.*;
import com.mockachu.util.RandomUtils;
import com.mockachu.util.RandomUtilsImpl;
import com.mockachu.validate.DataValidator;
import com.mockachu.validate.JsonDataValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public RandomUtils randomUtils() {
        return new RandomUtilsImpl();
    }

    @Bean
    public DataValidator jsonDataValidator() {
        return new JsonDataValidator(jsonSchemaFactory());
    }

    @Bean
    public ValueProducer valueProducer() {
        return new ValueProducerImpl(randomUtils());
    }

    @Bean
    public JsonProducer jsonProducer() {
        return new JsonProducerImpl(valueProducer(), randomUtils());
    }

    @Bean
    public JsonFromSchemaProducer jsonFromSchemaProducer() {
        return new JsonFromSchemaProducerImpl(valueProducer(), randomUtils());
    }

    @Bean
    public JsonSchemaFactory jsonSchemaFactory() {
        return JsonSchemaFactory.byDefault();
    }
}
