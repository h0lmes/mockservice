package com.mockservice.domain;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class CircularQueueScenarioStrategy implements ScenarioStrategy {

    public Optional<String> apply(List<Route> routes, Predicate<Route> condition) {
        int i = 0;
        Optional<Route> foundRoute = Optional.empty();
        while (i < routes.size() && foundRoute.isEmpty()) {
            Route route = routes.get(i);
            if (condition.test(route)) {
                foundRoute = Optional.of(route);
            } else {
                i++;
            }
        }

        if (foundRoute.isPresent()) {
            routes.remove(i);
            routes.add(foundRoute.get());
        }

        return foundRoute.map(Route::getAlt);
    }
}
