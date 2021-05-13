package com.mockservice.model;

public enum DataFileSource {
    RESOURCE("res"),
    FILE("file"),
    MEMORY("mem");

    private String name;

    DataFileSource(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
