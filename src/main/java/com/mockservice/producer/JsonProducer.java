package com.mockservice.producer;

public interface JsonProducer {
    String generate();

    String generate(JsonValueType rootElementType);
}
