package com.mockservice.util;

import java.util.Objects;

public class Pair<K, V> {

    private K key;
    private V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() { return key; }

    public V getValue() { return value; }

    @Override
    public String toString() {
        return key + " -> " + value;
    }

    @Override
    public int hashCode() {
        return key.hashCode() * 13 + (value == null ? 0 : value.hashCode());
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Pair) {
            Pair<K, V> pair = (Pair<K, V>) o;
            return Objects.equals(key, pair.key) && Objects.equals(value, pair.value);
        }
        return false;
    }
}
