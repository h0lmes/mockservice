package com.mockservice.util;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JsonFromSchemaGenerator {

    private static final int MIN_NUMBER_OF_ELEMENTS = 1;
    private static final int MAX_NUMBER_OF_ELEMENTS = 3;

    public static String jsonFromSchema(Map<String, Object> map) {
        if (!map.containsKey("type")) {
            return "null";
        }

        String type = map.get("type").toString();

        switch (type) {
            case "array":
                return makeArray(map);
            case "object":
                return makeObject(map);
            case "number":
                return makeNumber(map);
            case "integer":
                return makeInteger(map);
            case "string":
                return makeString(map);
            case "boolean":
                return makeBoolean(map);
        }
        return "null";
    }

    private static String makeArray(Map<String, Object> map) {
        Map<String, Object> items = (Map) map.get("items");
        int numberOfElements = RandomUtil.rnd(MIN_NUMBER_OF_ELEMENTS, MAX_NUMBER_OF_ELEMENTS);

        String content = IntStream.range(0, numberOfElements)
                .boxed()
                .map(i -> jsonFromSchema(items))
                .collect(Collectors.joining(", "));
        return "[" + content + "]";
    }

    private static String makeObject(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        String delimiter = "";

        Map<String, Object> properties = (Map) map.get("properties");
        if (properties != null) {
            for (Map.Entry<String, Object> e : properties.entrySet()) {
                String key = "\"" + e.getKey() + "\": " + jsonFromSchema((Map) e.getValue());
                sb.append(delimiter).append(key);
                delimiter = ", ";
            }
        }

        return sb.append("}").toString();
    }

    private static String makeNumber(Map<String, Object> map) {
        return ValueGenerator.randomNumberString();
    }

    private static String makeInteger(Map<String, Object> map) {
        return ValueGenerator.randomIntegerString();
    }

    private static String makeString(Map<String, Object> map) {
        List<Object> enumList = (List) map.get("enum");
        if (enumList != null) {
            int index = RandomUtil.rnd(enumList.size());
            return "\"" + enumList.get(index).toString() + "\"";
        }
        return "\"" + ValueGenerator.randomString() + "\"";
    }

    private static String makeBoolean(Map<String, Object> map) {
        return ValueGenerator.randomBooleanString();
    }
}
