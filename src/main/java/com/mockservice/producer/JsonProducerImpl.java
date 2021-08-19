package com.mockservice.producer;

import com.mockservice.util.RandomUtils;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class JsonProducerImpl implements JsonProducer {

    private static final int MAX_NUMBER_OF_ELEMENTS = 10;

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

    private final ValueProducer valueProducer;

    public JsonProducerImpl(ValueProducer valueProducer) {
        this.valueProducer = valueProducer;
    }

    @Override
    public String generate() {
        return generate(getRandomJsonValueType(rootValueTypes));
    }

    @Override
    public String generate(JsonValueType rootElementType) {
        return generateValue(rootElementType, 0);
    }

    private String generateValue(JsonValueType elementType, int level) {
        switch (elementType) {
            case STRING:
                return "\"" + valueProducer.randomString() + "\"";
            case NUMBER:
                return valueProducer.randomNumberString();
            case INTEGER:
                return valueProducer.randomIntegerString();
            case BOOLEAN:
                return valueProducer.randomBooleanString();
            case ARRAY:
                return generateArrayValue(level);
            case OBJECT:
                return generateObjectValue(level);
            default:
                return "null";
        }
    }

    private String generateArrayValue(int level) {
        if (stopAtLevel(level)) {
            return "null";
        }

        int numberOfElements = getRandomNumberOfElements();
        JsonValueType elementType = getRandomJsonValueType(valueTypes);

        if (JsonValueType.OBJECT.equals(elementType) || JsonValueType.ARRAY.equals(elementType)) {
            String content = createArrayElements(elementType, numberOfElements, level + 1, ",\n");
            return "[\n" + content + "\n" + padWithSpaces(level) + "]";
        }

        String content = createArrayElements(elementType, numberOfElements, 0, ", ");
        return "[" + content + "]";
    }

    private boolean stopAtLevel(int level) {
        int percent = 95 - level * 30;
        return percent < 10 || RandomUtils.withChance(100 - percent);
    }

    private int getRandomNumberOfElements() {
        return RandomUtils.rnd(MAX_NUMBER_OF_ELEMENTS + 1);
    }

    private JsonValueType getRandomJsonValueType(JsonValueType[] valueTypes) {
        return valueTypes[RandomUtils.rnd(valueTypes.length)];
    }

    private String createArrayElements(JsonValueType elementType,
                                              int numberOfElements,
                                              int elementsIndentationLevel,
                                              String elementsDelimiter) {
        return IntStream.range(0, numberOfElements)
                .boxed()
                .map(i -> makeArrayElement(elementType, elementsIndentationLevel))
                .collect(Collectors.joining(elementsDelimiter));
    }

    private String makeArrayElement(JsonValueType elementType, int level) {
        return padWithSpaces(level) + generateValue(elementType, level);
    }

    private String generateObjectValue(int level) {
        if (stopAtLevel(level)) {
            return "null";
        }

        int numberOfElements = getRandomNumberOfElements();
        return generateObjectValueInt(level, numberOfElements);
    }

    private String generateObjectValueInt(int level, int numberOfElements) {
        String content = Stream
                .generate(() -> valueProducer.randomWords(1))
                .distinct()
                .limit(numberOfElements)
                .map(keyName -> makeObjectElement(keyName, level + 1))
                .collect(Collectors.joining(",\n"));
        if (content.isEmpty()) {
            return "{}";
        }
        return "{\n" + content + "\n" + padWithSpaces(level) + "}";
    }

    private String makeObjectElement(String name, int level) {
        String value = generateValue(getRandomJsonValueType(valueTypes), level);
        return padWithSpaces(level) + makeKey(name, value);
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
