package com.mockachu.logging;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class LoggerThreadLocalMap {

    private static final ThreadLocal<LinkedHashMap<String, Object>> threadLocal = new ThreadLocal<>();

    private LoggerThreadLocalMap() {
        // private
    }

    public static void put(String key, Object val) {
        Objects.requireNonNull(key, "Key can not be null");

        var map = threadLocal.get();
        if (map == null) {
            map = new LinkedHashMap<>();
            threadLocal.set(map);
        }
        map.put(key, val);
    }

    @Nullable
    public static Object get(String key) {
        var map = threadLocal.get();
        return map != null && key != null ? map.get(key) : null;
    }

    public static void remove(String key) {
        var map = threadLocal.get();
        if (map != null) map.remove(key);
    }

    public static void remove(List<String> keys) {
        for (String key : keys) remove(key);
    }

    public static void clear() {
        var map = threadLocal.get();
        if (map != null) {
            map.clear();
            threadLocal.remove();
        }
    }

    public static Set<String> getKeys() {
        var map = threadLocal.get();
        return map != null ? map.keySet() : null;
    }
}
