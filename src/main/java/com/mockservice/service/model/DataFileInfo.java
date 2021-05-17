package com.mockservice.service.model;

public class DataFileInfo {

    private String name;
    private DataFileSource source;

    public DataFileInfo() {
        // for deserialization
    }

    public DataFileInfo(String name, DataFileSource source) {
        this.name = name;
        this.source = source;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataFileSource getSource() {
        return source;
    }

    public void setSource(DataFileSource source) {
        this.source = source;
    }
}
