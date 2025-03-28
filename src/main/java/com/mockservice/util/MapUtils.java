package com.mockservice.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.underscore.U;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

@SuppressWarnings("unchecked")
public class MapUtils {

    private MapUtils() {
        /* hidden */
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static Map<String, Object> jsonToMap(String json, ObjectMapper mapper) throws JsonProcessingException {
        if (json == null || json.isEmpty()) {
            return new HashMap<>();
        }
        return mapper.readValue(json, Map.class);
    }

    public static Map<String, Object> xmlToMap(String data) {
        if (data == null || data.isEmpty()) {
            return new HashMap<>();
        }
        Map<String, Object> map = U.fromXmlWithoutNamespaces(data);
        map = getXmlMapKeyAsMap(map, "envelope");
        return getXmlMapKeyAsMap(map, "body");
    }

    private static Map<String, Object> getXmlMapKeyAsMap(Map<String, Object> map, String key) {
        if (map != null) {
            for (Map.Entry<String, Object> e : map.entrySet()) {
                Object value = e.getValue();
                if (e.getKey().equalsIgnoreCase(key) && (value instanceof Map)) {
                    return (Map<String, Object>) value;
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

    private record TriEntry(String parent, String key, Object value) {
        String getParent() {
            return parent == null ? key : parent + "." + key;
        }
    }
}
