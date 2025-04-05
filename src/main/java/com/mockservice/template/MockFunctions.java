package com.mockservice.template;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class MockFunctions {
    private final Map<String, Function<String[], String>> map = new HashMap<>();

    public MockFunctions() {
        // default
    }

    public MockFunctions put(String key, Function<String[], String> value) {
        map.put(key, value);
        return this;
    }

    public Function<String[], String> get(String key) {
        return map.get(key);
    }

    public boolean containsKey(String key) {
        return map.containsKey(key);
    }
}
