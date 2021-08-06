package com.mockservice.util;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class JsonFromSchemaGenerator {

    private static final int MIN_NUMBER_OF_ELEMENTS = 1;
    private static final int MAX_NUMBER_OF_ELEMENTS = 3;

    private JsonFromSchemaGenerator() {
        /* hidden */
    }

    public static String jsonFromSchema(Map<String, Object> map) {
        return jsonFromSchema(map, 0);
    }

    private static String jsonFromSchema(Map<String, Object> map, int level) {
        String type = getJsonSchemaType(map);
        switch (type) {
            case "array":
                return makeArray(map, level);
            case "object":
                return makeObject(map, level);
            case "number":
                return ValueGenerator.randomNumberString();
            case "integer":
                return ValueGenerator.randomIntegerString();
            case "string":
                return makeString(map);
            case "boolean":
                return ValueGenerator.randomBooleanString();
            case "enum":
                return makeEnum(map);
            default:
                return "null";
        }
    }

    private static String getJsonSchemaType(Map<String, Object> map) {
        String type = String.valueOf(map.get("type"));
        if (map.containsKey("enum")) type = "enum";
        return type;
    }

    private static String padWithSpaces(int level) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < level; i++) builder.append("    ");
        return builder.toString();
    }

    private static String makeArray(Map<String, Object> map, int level) {
        Map<String, Object> items = (Map) map.get("items");
        int numberOfElements = RandomUtils.rnd(MIN_NUMBER_OF_ELEMENTS, MAX_NUMBER_OF_ELEMENTS);

        StringBuilder sb = new StringBuilder("[");
        String delimiter = "";
        for (int i = 0; i < numberOfElements; i++) {
            sb.append(delimiter);
            sb.append(jsonFromSchema(items, level + 1));
            delimiter = ", ";
        }
        return sb.append("]").toString();
    }

    private static String makeObject(Map<String, Object> map, int level) {
        StringBuilder sb = new StringBuilder("{");
        String delimiter = "";

        Map<String, Object> properties = (Map) map.get("properties");
        if (properties != null) {
            for (Map.Entry<String, Object> e : properties.entrySet()) {
                sb.append(delimiter);
                sb.append("\n");
                sb.append(makeObjectKey(e.getKey(), e.getValue(),  level + 1));
                delimiter = ",";
            }
        }

        return sb
                .append("\n")
                .append(padWithSpaces(level))
                .append("}")
                .toString();
    }

    private static String makeObjectKey(String key, Object value, int level) {
        return padWithSpaces(level)
                + "\""
                + key
                + "\": "
                + jsonFromSchema((Map) value, level);
    }

    private static String makeString(Map<String, Object> map) {
        if (map.containsKey("example")) return makeStringFromExample(map);
        if (map.containsKey("format")) return makeStringFromFormat(map);
        return "\"" + ValueGenerator.randomString() + "\"";
    }

    private static String makeStringFromExample(Map<String, Object> map) {
        return "\"" + map.get("example").toString() + "\"";
    }

    private static String makeStringFromFormat(Map<String, Object> map) {
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
                return "";
        }
    }

    private static String makeEnum(Map<String, Object> map) {
        List<Object> enumList = (List) map.get("enum");
        if (enumList != null) return getRandomItem(enumList);
        return "null";
    }

    private static String getRandomItem(List<Object> enumList) {
        int index = RandomUtils.rnd(enumList.size());
        Object item = enumList.get(index);
        String quote = item instanceof String ? "\"" : "";
        return quote + item.toString() + quote;
    }
}
