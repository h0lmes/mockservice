package com.mockservice.model;

public class PlainConfig {

    private String data = "";
    private boolean file = false;
    private boolean resource = false;

    public PlainConfig() {
        // default
    }

    public String getData() {
        return data;
    }

    public PlainConfig setData(String data) {
        this.data = data;
        return this;
    }

    public boolean getFile() {
        return file;
    }

    public PlainConfig setFile(boolean file) {
        this.file = file;
        return this;
    }

    public boolean getResource() {
        return resource;
    }

    public PlainConfig setResource(boolean resource) {
        this.resource = resource;
        return this;
    }
}
