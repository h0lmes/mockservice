package com.mockservice.util;

import java.util.List;
import java.util.Map;

public class JsonFromSchemaGenerator {

    private static final int MIN_NUMBER_OF_ELEMENTS = 1;
    private static final int MAX_NUMBER_OF_ELEMENTS = 3;

    private JsonFromSchemaGenerator() {
        // default
    }

    public static String jsonFromSchema(Map<String, Object> map) {
        return jsonFromSchema(map, 0);
    }

    @SuppressWarnings("unchecked")
    public static String jsonFromSchema(Map<String, Object> map, int level) {
        if (!map.containsKey("type")) {
            return "null";
        }

        String type = String.valueOf(map.get("type"));

        if (map.containsKey("enum")) {
            type = "enum";
        }

        switch (type) {
            case "array":
                return makeArray(map, level);
            case "object":
                return makeObject(map, level);
            case "number":
                return makeNumber(map, level);
            case "integer":
                return makeInteger(map, level);
            case "string":
                return makeString(map, level);
            case "boolean":
                return makeBoolean(map, level);
            case "enum":
                return makeEnum(map, level);
            default:
                return makeNull(map, level);
        }
    }

    private static String offset(int level) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < level; i++) {
            builder.append("    ");
        }
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    private static String makeArray(Map<String, Object> map, int level) {
        Map<String, Object> items = (Map) map.get("items");
        int numberOfElements = RandomUtils.rnd(MIN_NUMBER_OF_ELEMENTS, MAX_NUMBER_OF_ELEMENTS);

        StringBuilder sb = new StringBuilder("[");
        String delimiter = "";

        for (int i = 0; i < numberOfElements; i++) {
            sb.append(delimiter)
                    .append(jsonFromSchema(items, level + 1));
            delimiter = ", ";
        }

        return sb.append("]").toString();
    }

    @SuppressWarnings("unchecked")
    private static String makeObject(Map<String, Object> map, int level) {
        StringBuilder sb = new StringBuilder("{");
        String delimiter = "";

        Map<String, Object> properties = (Map) map.get("properties");
        if (properties != null) {
            for (Map.Entry<String, Object> e : properties.entrySet()) {
                sb
                        .append(delimiter)
                        .append("\n")
                        .append(offset(level + 1))
                        .append("\"")
                        .append(e.getKey())
                        .append("\": ")
                        .append(jsonFromSchema((Map) e.getValue(), level + 1));
                delimiter = ",";
            }
        }

        return sb
                .append("\n")
                .append(offset(level))
                .append("}")
                .toString();
    }

    @SuppressWarnings("unchecked")
    private static String makeNumber(Map<String, Object> map, int level) {
        return ValueGenerator.randomNumberString();
    }

    @SuppressWarnings("unchecked")
    private static String makeInteger(Map<String, Object> map, int level) {
        return ValueGenerator.randomIntegerString();
    }

    @SuppressWarnings("unchecked")
    private static String makeString(Map<String, Object> map, int level) {
        if (map.containsKey("example")) {
            return "\"" + map.get("example").toString() + "\"";
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

    private static String makeBoolean(Map<String, Object> map, int level) {
        return ValueGenerator.randomBooleanString();
    }

    @SuppressWarnings("unchecked")
    private static String makeEnum(Map<String, Object> map, int level) {
        List<Object> enumList = (List) map.get("enum");
        if (enumList != null) {
            int index = RandomUtils.rnd(enumList.size());
            if (enumList.get(index) instanceof String) {
                return "\"" + enumList.get(index).toString() + "\"";
            } else {
                return enumList.get(index).toString();
            }
        }

        return "null";
    }

    private static String makeNull(Map<String, Object> map, int level) {
        return "null";
    }
}
