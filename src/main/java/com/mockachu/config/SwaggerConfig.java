package com.mockachu.config;

//import io.swagger.v3.oas.models.media.Schema;
//
//import java.util.Collections;
//import java.util.Set;

@SuppressWarnings("java:S125")
public class SwaggerConfig {

    // make springdoc generate Array schema for Set.class (remove uniqueItems: true)
    public SwaggerConfig() {
//        var schema = new Schema<Set<?>>();
//        schema.type("array").example(Collections.emptyList());
//        SpringDocUtils.getConfig().replaceWithSchema(Set.class, schema);
    }
}
