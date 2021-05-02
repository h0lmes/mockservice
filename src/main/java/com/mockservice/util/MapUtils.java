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

    @SuppressWarnings("unchecked")
    public static Map<String, Object> jsonToMap(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule module = new JavaTimeModule();
        mapper.registerModule(module);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        return mapper.readValue(json, Map.class);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> flattenMap(Map<String, Object> map) {
        Queue<Pair<String, Pair<String, Object>>> q = new ArrayDeque<>();
        Map<String, String> result = new HashMap<>();

        map.forEach((k, v) -> q.offer(new Pair<>(null, new Pair<>(k, v))));

        while (!q.isEmpty()) {
            Pair<String, Pair<String, Object>> el = q.poll();
            String key = el.getKey() == null ? el.getValue().getKey() : el.getKey() + "." + el.getValue().getKey();
            Object obj = el.getValue().getValue();
            if (obj instanceof Map) {
                ((Map<String, Object>) obj).forEach((k, v) -> q.offer(new Pair<>(key, new Pair<>(k, v))));
            } else {
                result.put(key, obj == null ? "null" : obj.toString());
            }
        }
        return result;
    }
}
