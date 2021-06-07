package com.mockservice.domain;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class ScenarioQueueStrategy implements ScenarioStrategy {

    public Optional<String> apply(List<Route> routes, Predicate<Route> condition) {
        int i = 0;
        Optional<String> alt = Optional.empty();
        while (i < routes.size() && alt.isEmpty()) {
            Route route = routes.get(i);
            if (condition.test(route)) {
                alt = Optional.of(route.getAlt());
            } else {
                i++;
            }
        }

        if (alt.isPresent()) {
            routes.remove(i);
        }

        return alt;
    }
}
