package com.mockservice.service.model;

public class DataFileInfo {

    private String group;
    private String name;
    private String path;
    private DataFileSource source;

    public DataFileInfo() {
        // for deserialization
    }

    public DataFileInfo(String group, String name, String path, DataFileSource source) {
        this.group = group;
        this.name = name;
        this.path = path;
        this.source = source;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public DataFileSource getSource() {
        return source;
    }

    public void setSource(DataFileSource source) {
        this.source = source;
    }
}
