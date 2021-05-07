package com.mockservice.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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

    @SuppressWarnings("unchecked")
    public static Map<String, String> flattenMap(Map<String, Object> map) {
        Map<String, String> result = new HashMap<>();

        Queue<TriEntry> queue = new ArrayDeque<>();
        map.forEach((k, v) -> queue.offer(new TriEntry(null, k, v)));

        while (!queue.isEmpty()) {
            TriEntry entry = queue.poll();
            String key = entry.getFullKey();
            Object value = entry.value;
            if (value instanceof Map) {
                ((Map<String, Object>) value).forEach((k, v) -> queue.offer(new TriEntry(key, k, v)));
            } else {
                result.put(key, value == null ? null : value.toString());
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

        String getFullKey() {
            return parent == null ? key : parent + "." + key;
        }
    }
}
