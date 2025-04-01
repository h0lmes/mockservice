package com.mockservice.components;

public interface JsonProducer {
    String generate();
    String generate(JsonValueType rootElementType);
    String generateArray(int level, int numberOfElements, JsonValueType elementType);
}
