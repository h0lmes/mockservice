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

    public static String generate() {
        return generate(rootValueTypes[RandomUtil.rnd(rootValueTypes.length)]);
    }

    public static String generate(JsonValueType rootElementType) {
        return generateValue(rootElementType, 0);
    }

    private static String generateValue(JsonValueType elementType, int level) {
        switch (elementType) {
            case STRING:
                return generateStringValue();
            case NUMBER:
                return generateNumberValue();
            case INTEGER:
                return generateIntegerValue();
            case BOOLEAN:
                return generateBooleanValue();
            case ARRAY:
                return generateArrayValue(level);
            case OBJECT:
                return generateObjectValue(level);
            default:
                return "null";
        }
    }

    private static String generateStringValue() {
        return "\"" + ValueGenerator.randomString() + "\"";
    }

    private static String generateIntegerValue() {
        return ValueGenerator.randomIntegerString();
    }

    private static String generateNumberValue() {
        return ValueGenerator.randomNumberString();
    }

    private static String generateBooleanValue() {
        return ValueGenerator.randomBooleanString();
    }

    private static String generateArrayValue(int level) {
        int percent = 95 - level * 30;
        if (percent < 10 || RandomUtil.withChance(100 - percent)) {
            return "null";
        }

        int numberOfElements = RandomUtil.rnd(MAX_NUMBER_OF_ELEMENTS + 1);
        JsonValueType elementType = valueTypes[RandomUtil.rnd(valueTypes.length)];

        if (JsonValueType.OBJECT.equals(elementType) || JsonValueType.ARRAY.equals(elementType)) {
            String content = IntStream.range(0, numberOfElements)
                    .boxed()
                    .map(i -> makeArrayElement(elementType, level + 1))
                    .collect(Collectors.joining(",\n"));
            return "[\n" + content + "\n" + offset(level) + "]";
        }

        String content = IntStream.range(0, numberOfElements)
                .boxed()
                .map(i -> makeArrayElement(elementType, 0))
                .collect(Collectors.joining(", "));
        return "[" + content + "]";
    }

    private static String makeArrayElement(JsonValueType elementType, int level) {
        return offset(level) + generateValue(elementType, level);
    }

    private static String generateObjectValue(int level) {
        int percent = 95 - level * 20;
        if (percent < 10 || RandomUtil.withChance(100 - percent)) {
            return "null";
        }

        int numberOfElements = RandomUtil.rnd(MAX_NUMBER_OF_ELEMENTS + 1);
        String content = Stream
                .generate(() -> ValueGenerator.randomWords(1))
                .distinct()
                .limit(numberOfElements)
                .map(keyName -> makeObjectElement(keyName, level + 1))
                .collect(Collectors.joining(",\n"));
        if (content.isEmpty()) {
            return "{}";
        }
        return "{\n" + content + "\n" + offset(level) + "}";
    }

    private static String makeObjectElement(String name, int level) {
        return offset(level) + makeKey(name, generateValue(valueTypes[RandomUtil.rnd(valueTypes.length)], level));
    }

    private static String makeKey(String name, String value) {
        return "\"" + name + "\": " + value;
    }

    private static String offset(int level) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < level; i++) {
            builder.append("    ");
        }
        return builder.toString();
    }
}
