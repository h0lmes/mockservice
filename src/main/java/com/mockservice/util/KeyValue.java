package com.mockservice.util;

public class KeyValue {
    private String key;
    private String value = "";

    public static KeyValue of(String keyValue, String delimiter) {
        return new KeyValue(keyValue, delimiter);
    }

    public KeyValue(String keyValue, String delimiter) {
        int index = keyValue.indexOf(delimiter);
        if (index < 0) {
            key = keyValue;
            return;
        }
        key = keyValue.substring(0, index).trim();
        if (keyValue.length() > index + delimiter.length()) {
            value = keyValue.substring(index + delimiter.length()).trim();
        }
    }

    public String key() {
        return key;
    }

    public String value() {
        return value;
    }
}
