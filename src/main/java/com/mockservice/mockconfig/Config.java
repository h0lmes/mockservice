package com.mockservice.mockconfig;

import java.util.List;

public class Config {

    private List<Group> groups;

    public Config() {
        // default
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }
}
