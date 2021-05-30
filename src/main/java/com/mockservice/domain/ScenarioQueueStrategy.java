package com.mockservice.domain;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class ScenarioQueueStrategy implements ScenarioStrategy {

    public Optional<String> apply(List<Route> routes, Predicate<Route> condition) {
        int i = 0;
        Optional<String> suffix = Optional.empty();
        while (i < routes.size() && suffix.isEmpty()) {
            Route route = routes.get(i);
            if (condition.test(route)) {
                suffix = Optional.of(route.getSuffix());
            } else {
                i++;
            }
        }

        if (suffix.isPresent()) {
            routes.remove(i);
        }

        return suffix;
    }
}
