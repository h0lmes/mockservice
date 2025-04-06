package com.mockachu.components;

import com.mockachu.util.RandomUtils;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class JsonFromSchemaProducerImpl implements JsonFromSchemaProducer {

    private static final int MIN_NUMBER_OF_ELEMENTS = 1;
    private static final int MAX_NUMBER_OF_ELEMENTS = 3;

    private final ValueProducer valueProducer;
    private final RandomUtils randomUtils;

    public JsonFromSchemaProducerImpl(ValueProducer valueProducer, RandomUtils randomUtils) {
        this.valueProducer = valueProducer;
        this.randomUtils = randomUtils;
    }

    @Override
    public String jsonFromSchema(Map<String, Object> map) {
        return jsonFromSchema(map, 0);
    }

    private String jsonFromSchema(Map<String, Object> map, int level) {
        String type = getJsonSchemaType(map);
        return switch (type) {
            case "array" -> makeArray(map, level);
            case "object" -> makeObject(map, level);
            case "number" -> valueProducer.randomNumberString();
            case "integer" -> valueProducer.randomIntegerString();
            case "string" -> makeString(map);
            case "boolean" -> valueProducer.randomBooleanString();
            case "enum" -> makeEnum(map);
            default -> "null";
        };
    }

    private String getJsonSchemaType(Map<String, Object> map) {
        String type = String.valueOf(map.get("type"));
        if (map.containsKey("enum")) type = "enum";
        return type;
    }

    private String padWithSpaces(int level) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < level; i++) builder.append("    ");
        return builder.toString();
    }

    private String makeArray(Map<String, Object> map, int level) {
        Map<String, Object> items = (Map) map.get("items");
        int numberOfElements = randomUtils.rnd(MIN_NUMBER_OF_ELEMENTS, MAX_NUMBER_OF_ELEMENTS);

        StringBuilder sb = new StringBuilder("[");
        String delimiter = "";
        for (int i = 0; i < numberOfElements; i++) {
            sb.append(delimiter);
            sb.append(jsonFromSchema(items, level + 1));
            delimiter = ", ";
        }
        return sb.append("]").toString();
    }

    private String makeObject(Map<String, Object> map, int level) {
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

    private String makeObjectKey(String key, Object value, int level) {
        return padWithSpaces(level)
                + "\""
                + key
                + "\": "
                + jsonFromSchema((Map) value, level);
    }

    private String makeString(Map<String, Object> map) {
        if (map.containsKey("example")) return makeStringFromExample(map);
        if (map.containsKey("format")) return makeStringFromFormat(map);
        return "\"" + valueProducer.randomString() + "\"";
    }

    private String makeStringFromExample(Map<String, Object> map) {
        return "\"" + map.get("example").toString() + "\"";
    }

    private String makeStringFromFormat(Map<String, Object> map) {
        String format = String.valueOf(map.get("format"));
        return switch (format) {
            case "date-time" -> "\"2018-11-13T20:20:39+00:00\"";
            case "time" -> "\"20:20:39+00:00\"";
            case "date" -> "\"2018-11-13\"";
            case "email" -> "\"example@gmail.com\"";
            case "hostname" -> "\"some-domain.com\"";
            case "ipv4" -> "\"127.0.0.1\"";
            case "ipv6" -> "\"::1\"";
            case "uri" -> "\"http://some-domain.com/\"";
            default -> "";
        };
    }

    private String makeEnum(Map<String, Object> map) {
        List<Object> enumList = (List) map.get("enum");
        if (enumList != null) return getRandomItem(enumList);
        return "null";
    }

    private String getRandomItem(List<Object> enumList) {
        int index = randomUtils.rnd(enumList.size());
        Object item = enumList.get(index);
        String quote = item instanceof String ? "\"" : "";
        return quote + item.toString() + quote;
    }
}
