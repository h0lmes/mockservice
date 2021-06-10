package com.mockservice.quantum;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JsonGenerator {

    private static final int MAX_NUMBER_OF_ELEMENTS = 10;

    enum JsonValueType {
        NUMBER, STRING, BOOLEAN, ARRAY, OBJECT
    }

    private static final JsonValueType[] rootValueTypes = {
            JsonValueType.ARRAY,
            JsonValueType.OBJECT
    };
    private static final JsonValueType[] valueTypes = {
            JsonValueType.NUMBER,
            JsonValueType.STRING,
            JsonValueType.BOOLEAN,
            JsonValueType.ARRAY,
            JsonValueType.OBJECT
    };

    public static String generate() {
        JsonValueType rootElementType = rootValueTypes[RandomUtil.rnd(rootValueTypes.length)];
        return generateValue(rootElementType, 0);
    }

    private static String generateValue(JsonValueType elementType, int level) {
        switch (elementType) {
            case NUMBER:
                return generateNumberValue();
            case STRING:
                return generateStringValue();
            case BOOLEAN:
                return generateBooleanValue();
            case ARRAY:
                return generateArrayValue(level);
            case OBJECT:
                return generateObjectValue(level);
            default:
                return "";
        }
    }

    private static String generateNumberValue() {
        return ValueGenerator.randomNumberString();
    }

    private static String generateStringValue() {
        return "\"" + ValueGenerator.randomString() + "\"";
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
        String content = IntStream.range(0, numberOfElements)
                .boxed()
                .map(i -> offset(level + 1) + generateValue(elementType, level + 1))
                .collect(Collectors.joining(", \n"));
        return "[\n" + content + "\n" + offset(level) + "]";
    }

    private static String generateObjectValue(int level) {
        int percent = 95 - level * 20;
        if (percent < 10 || RandomUtil.withChance(100 - percent)) {
            return "null";
        }

        int numberOfElements = RandomUtil.rnd(MAX_NUMBER_OF_ELEMENTS + 1);
        String content = IntStream.range(0, numberOfElements)
                .boxed()
                .map(i -> offset(level + 1) + makeKey(generateValue(valueTypes[RandomUtil.rnd(valueTypes.length)], level + 1)))
                .collect(Collectors.joining(", \n"));
        return "{\n" + content + "\n" + offset(level) + "}";
    }

    private static String makeKey(String value) {
        return "\"" + generateKeyName() + "\": " + value;
    }

    private static String generateKeyName() {
        return ValueGenerator.randomWords(1).replaceAll("\\W", "");
    }

    private static String offset(int level) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < level; i++) {
            builder.append("    ");
        }
        return builder.toString();
    }
}
