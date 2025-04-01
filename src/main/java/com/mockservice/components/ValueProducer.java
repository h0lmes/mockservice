package com.mockservice.components;

public interface ValueProducer {
    String randomString();
    String randomWords(int numberOfWords);
    String randomNumberString();
    String randomIntegerString();
    String randomBooleanString();
}
