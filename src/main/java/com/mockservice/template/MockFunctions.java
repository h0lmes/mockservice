package com.mockservice.template;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class MockFunctions {
    private final Map<String, Function<String[], String>> map = new HashMap<>();

    public MockFunctions() {
        // default
    }

    public void clear() {
        map.clear();
    }

    public void put(String key, Function<String[], String> value) {
        map.put(key, value);
    }

    public Function<String[], String> get(String key) {
        return map.get(key);
    }

    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    Map<String, Function<String[], String>> getAll() {
        return map;
    }

    public void putAll(@Nullable MockFunctions functions) {
        if (functions != null) {
            map.putAll(functions.getAll());
        }
    }
}
