package com.mockservice.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.underscore.lodash.U;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class MapUtils {

    private MapUtils() {
        // hidden
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> jsonToMap(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        return mapper.readValue(json, Map.class);
    }

    public static Map<String, Object> xmlToMap(String body) {
        Map<String, Object> map = U.fromXmlWithoutNamespaces(body);
        map = getXmlToJsonMapKeyAsMap(map, "envelope");
        return getXmlToJsonMapKeyAsMap(map, "body");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getXmlToJsonMapKeyAsMap(Map<String, Object> map, String key) {
        if (map != null) {
            for (String k : map.keySet()) {
                if (k.equalsIgnoreCase(key) && (map.get(k) instanceof Map)) {
                    return (Map<String, Object>) map.get(k);
                }
            }
        }
        return new HashMap<>();
    }

    public static Map<String, String> flattenMap(Map<String, Object> map) {
        Queue<TriEntry> queue = new ArrayDeque<>();
        map.forEach((k, v) -> queue.offer(new TriEntry(null, k, v)));
        return runBfs(queue);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> runBfs(Queue<TriEntry> queue) {
        Map<String, String> result = new HashMap<>();
        while (!queue.isEmpty()) {
            TriEntry entry = queue.poll();
            Object value = entry.value;
            if (value instanceof Map) {
                ((Map<String, Object>) value).forEach((k, v) ->
                        queue.offer(new TriEntry(entry.getParent(), k, v))
                );
            } else {
                result.put(entry.getParent(), value == null ? null : value.toString());
            }
        }
        return result;
    }

    private static class TriEntry {
        private String parent;
        private String key;
        private Object value;

        TriEntry(String parent, String key, Object value) {
            this.parent = parent;
            this.key = key;
            this.value = value;
        }

        String getParent() {
            return parent == null ? key : parent + "." + key;
        }
    }
}
