package com.mockservice.mockconfig;

import java.util.ArrayList;
import java.util.List;

public class Config {

    private List<Group> groups = new ArrayList<>();

    public Config() {
        // default
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public Group getOrCreateGroup(String groupName) {
        Group maybeGroup = groups.stream()
                .filter(group -> groupName.equalsIgnoreCase(group.getName()))
                .findFirst()
                .orElse(null);
        if (maybeGroup == null) {
            maybeGroup = new Group().setName(groupName);
            groups.add(maybeGroup);
        }
        return maybeGroup;
    }
}
