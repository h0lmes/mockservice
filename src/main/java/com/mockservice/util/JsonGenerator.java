package com.mockservice.util;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class JsonGenerator {

    private static final int MAX_NUMBER_OF_ELEMENTS = 10;

    public enum JsonValueType {
        STRING, NUMBER, INTEGER, BOOLEAN, NULL, ARRAY, OBJECT
    }

    private static final JsonValueType[] rootValueTypes = {
            JsonValueType.OBJECT,
            JsonValueType.OBJECT,
            JsonValueType.OBJECT,
            JsonValueType.OBJECT,
            JsonValueType.ARRAY,
    };
    private static final JsonValueType[] valueTypes = {
            JsonValueType.NUMBER,
            JsonValueType.INTEGER,
            JsonValueType.STRING,
            JsonValueType.STRING,
            JsonValueType.BOOLEAN,
            JsonValueType.BOOLEAN,
            JsonValueType.NULL,
            JsonValueType.ARRAY,
            JsonValueType.OBJECT,
            JsonValueType.OBJECT,
            JsonValueType.OBJECT,
    };

    private JsonGenerator() {
        /* hidden */
    }

    public static String generate() {
        return generate(rootValueTypes[RandomUtils.rnd(rootValueTypes.length)]);
    }

    public static String generate(JsonValueType rootElementType) {
        return generateValue(rootElementType, 0);
    }

    private static String generateValue(JsonValueType elementType, int level) {
        switch (elementType) {
            case STRING:
                return "\"" + ValueGenerator.randomString() + "\"";
            case NUMBER:
                return ValueGenerator.randomNumberString();
            case INTEGER:
                return ValueGenerator.randomIntegerString();
            case BOOLEAN:
                return ValueGenerator.randomBooleanString();
            case ARRAY:
                return generateArrayValue(level);
            case OBJECT:
                return generateObjectValue(level);
            default:
                return "null";
        }
    }

    private static String generateArrayValue(int level) {
        int percent = 95 - level * 30;
        if (percent < 10 || RandomUtils.withChance(100 - percent)) {
            return "null";
        }

        int numberOfElements = RandomUtils.rnd(MAX_NUMBER_OF_ELEMENTS + 1);
        JsonValueType elementType = valueTypes[RandomUtils.rnd(valueTypes.length)];

        if (JsonValueType.OBJECT.equals(elementType) || JsonValueType.ARRAY.equals(elementType)) {
            String content = createArrayElements(elementType, numberOfElements, level + 1, ",\n");
            return "[\n" + content + "\n" + padWithSpaces(level) + "]";
        }

        String content = createArrayElements(elementType, numberOfElements, 0, ", ");
        return "[" + content + "]";
    }

    private static String createArrayElements(JsonValueType elementType,
                                              int numberOfElements,
                                              int elementsIndentationLevel,
                                              String elementsDelimiter) {
        return IntStream.range(0, numberOfElements)
                .boxed()
                .map(i -> makeArrayElement(elementType, elementsIndentationLevel))
                .collect(Collectors.joining(elementsDelimiter));
    }

    private static String makeArrayElement(JsonValueType elementType, int level) {
        return padWithSpaces(level) + generateValue(elementType, level);
    }

    private static String generateObjectValue(int level) {
        int percent = 95 - level * 20;
        if (percent < 10 || RandomUtils.withChance(100 - percent)) {
            return "null";
        }

        int numberOfElements = RandomUtils.rnd(MAX_NUMBER_OF_ELEMENTS + 1);
        String content = Stream
                .generate(() -> ValueGenerator.randomWords(1))
                .distinct()
                .limit(numberOfElements)
                .map(keyName -> makeObjectElement(keyName, level + 1))
                .collect(Collectors.joining(",\n"));
        if (content.isEmpty()) return "{}";
        return "{\n" + content + "\n" + padWithSpaces(level) + "}";
    }

    private static String makeObjectElement(String name, int level) {
        return padWithSpaces(level) + makeKey(name, generateValue(valueTypes[RandomUtils.rnd(valueTypes.length)], level));
    }

    private static String makeKey(String name, String value) {
        return "\"" + name + "\": " + value;
    }

    private static String padWithSpaces(int level) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < level; i++) builder.append("    ");
        return builder.toString();
    }
}
