package com.mockservice.util;

import java.util.List;
import java.util.Map;

public class JsonFromSchemaGenerator {

    private static final int MIN_NUMBER_OF_ELEMENTS = 1;
    private static final int MAX_NUMBER_OF_ELEMENTS = 3;

    private JsonFromSchemaGenerator() {
        // default
    }

    @SuppressWarnings("unchecked")
    public static String jsonFromSchema(Map<String, Object> map) {
        if (!map.containsKey("type")) {
            return "null";
        }

        String type = String.valueOf(map.get("type"));

        if (map.containsKey("enum")) {
            type = "enum";
        }

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
                return makeBoolean();
            case "enum":
                return makeEnum(map);
            default:
                return "null";
        }
    }

    @SuppressWarnings("unchecked")
    private static String makeArray(Map<String, Object> map) {
        Map<String, Object> items = (Map) map.get("items");
        int numberOfElements = RandomUtil.rnd(MIN_NUMBER_OF_ELEMENTS, MAX_NUMBER_OF_ELEMENTS);

        StringBuilder sb = new StringBuilder("[");
        String delimiter = "";

        for (int i = 0; i < numberOfElements; i++) {
            sb.append(delimiter)
                    .append(jsonFromSchema(items));
            delimiter = ", ";
        }

        return sb.append("]").toString();
    }

    @SuppressWarnings("unchecked")
    private static String makeObject(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        String delimiter = "";

        Map<String, Object> properties = (Map) map.get("properties");
        if (properties != null) {
            for (Map.Entry<String, Object> e : properties.entrySet()) {
                sb.append(delimiter)
                        .append("\"")
                        .append(e.getKey())
                        .append("\": ")
                        .append(jsonFromSchema((Map) e.getValue()));
                delimiter = ", ";
            }
        }

        return sb.append("}").toString();
    }

    @SuppressWarnings("unchecked")
    private static String makeNumber(Map<String, Object> map) {
        return ValueGenerator.randomNumberString();
    }

    @SuppressWarnings("unchecked")
    private static String makeInteger(Map<String, Object> map) {
        return ValueGenerator.randomIntegerString();
    }

    @SuppressWarnings("unchecked")
    private static String makeString(Map<String, Object> map) {
        String example = String.valueOf(map.get("example"));
        if (!example.isEmpty()) {
            return "\"" + example + "\"";
        }

        String format = String.valueOf(map.get("format"));
        switch (format) {
            case "date-time":
                return "\"2018-11-13T20:20:39+00:00\"";
            case "time":
                return "\"20:20:39+00:00\"";
            case "date":
                return "\"2018-11-13\"";
            case "email":
                return "\"example@gmail.com\"";
            case "hostname":
                return "\"some-domain.com\"";
            case "ipv4":
                return "\"127.0.0.1\"";
            case "ipv6":
                return "\"::1\"";
            case "uri":
                return "\"http://some-domain.com/\"";
            default:
                return "\"" + ValueGenerator.randomString() + "\"";
        }
    }

    private static String makeBoolean() {
        return ValueGenerator.randomBooleanString();
    }

    @SuppressWarnings("unchecked")
    private static String makeEnum(Map<String, Object> map) {
        List<Object> enumList = (List) map.get("enum");
        if (enumList != null) {
            int index = RandomUtil.rnd(enumList.size());
            if (enumList.get(index) instanceof String) {
                return "\"" + enumList.get(index).toString() + "\"";
            } else {
                return enumList.get(index).toString();
            }
        }

        return "null";
    }
}
