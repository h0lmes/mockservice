package com.mockservice.mockconfig;

import java.util.ArrayList;
import java.util.List;

public class Group {

    private String name = "";
    private List<Route> routes = new ArrayList<>();

    public Group() {
        // default
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }
}
