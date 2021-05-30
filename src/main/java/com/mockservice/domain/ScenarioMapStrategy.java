package com.mockservice.domain;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class ScenarioMapStrategy implements ScenarioStrategy {

    public Optional<String> apply(List<Route> routes, Predicate<Route> condition) {
        return routes.stream().filter(condition).map(Route::getSuffix).findFirst();
    }
}
