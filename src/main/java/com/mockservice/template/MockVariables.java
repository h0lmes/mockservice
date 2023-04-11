package com.mockservice.template;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class MockVariables {
    private final Map<String, String> map = new HashMap<>();

    public MockVariables() {
        // default
    }

    public void clear() {
        map.clear();
    }

    public void put(String key, String value) {
        map.put(key, value);
    }

    public String get(String key) {
        return map.get(key);
    }

    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    public String remove(String key) {
        return map.remove(key);
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public int size() {
        return map.size();
    }

    Map<String, String> getAll() {
        return map;
    }

    public void putAll(@Nullable MockVariables variables) {
        if (variables != null) {
            map.putAll(variables.getAll());
        }
    }

    public void putAll(@Nullable Map<String, String> map) {
        if (map != null) {
            this.map.putAll(map);
        }
    }
}
