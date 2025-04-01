package com.mockservice.template;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class MockVariables {
    private final Map<String, String> map;

    public static MockVariables empty() {
        return new MockVariables();
    }

    /**
     * Does not create a Map. Uses the provided one.
     */
    public static MockVariables of(Map<String, String> map) {
        return new MockVariables(map);
    }

    public static String get(@Nullable MockVariables variables, String name) {
        if (variables == null || variables.isEmpty()) return "null";
        String v = variables.get(name);
        return v == null ? "null" : v;
    }

    public static MockVariables sum(@Nullable MockVariables first,
                                    @Nullable MockVariables second) {
        if (second!= null && first!= null &&
                !second.isEmpty() && !first.isEmpty()) {
            return new MockVariables().putAll(first).putAll(second);
        }
        if (second!= null && !second.isEmpty()) {
            return second;
        }
        if (first!= null && !first.isEmpty()) {
            return first;
        }
        return null;
    }

    public MockVariables() {
        map = new HashMap<>();
    }

    public MockVariables(Map<String, String> map) {
        this.map = map;
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
