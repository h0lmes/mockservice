package com.mockservice.domain;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@FunctionalInterface
public interface ScenarioStrategy {
    Optional<String> apply(List<Route> routes, Predicate<Route> condition);
}
