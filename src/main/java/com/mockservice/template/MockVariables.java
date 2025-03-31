package com.mockservice.template;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class MockVariables {
    private final Map<String, String> map = new HashMap<>();

    public static MockVariables empty() {
        return new MockVariables();
    }

    public MockVariables() {
        // default
    }

    public MockVariables clear() {
        map.clear();
        return this;
    }

    public MockVariables put(String key, String value) {
        map.put(key, value);
        return this;
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

    public MockVariables putAll(@Nullable MockVariables variables) {
        if (variables != null) {
            map.putAll(variables.getAll());
        }
        return this;
    }

    public MockVariables putAll(@Nullable Map<String, String> map) {
        if (map != null) {
            this.map.putAll(map);
        }
        return this;
    }

    public MockVariables forEach(BiConsumer<? super String, ? super String> consumer) {
        getAll().forEach(consumer);
        return this;
    }

    public MockVariables forEachSorted(BiConsumer<? super String, ? super String> consumer) {
        getAll().keySet().stream().sorted().forEach(k -> consumer.accept(k, getAll().get(k)));
        return this;
    }
}
