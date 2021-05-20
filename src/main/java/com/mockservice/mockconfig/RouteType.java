package com.mockservice.mockconfig;

public enum RouteType {
    REST(".json"),
    SOAP(".xml");

    private final String ext;

    RouteType(String ext) {
        this.ext = ext;
    }

    public String getExt() {
        return ext;
    }

    public static RouteType ofExt(String ext) {
        if (REST.getExt().equalsIgnoreCase(ext)) {
            return REST;
        }
        return SOAP;
    }
}
