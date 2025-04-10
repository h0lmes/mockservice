package com.mockachu.domain;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class MapScenarioStrategy implements ScenarioStrategy {

    public Optional<String> apply(List<Route> routes, Predicate<Route> condition) {
        for (Route route : routes) {
            if (condition.test(route)) {
                return Optional.of(route.getAlt());
            }
        }
        return Optional.empty();
    }
}
