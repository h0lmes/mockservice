package com.mockservice.util;

import org.junit.jupiter.api.RepeatedTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class RandomUtilsTest {

    private static final int MIN = 5;
    private static final int MAX = 10;

    @RepeatedTest(10)
    public void rnd_NumberOfAlternatives_ReturnsNumberNotLessThanZeroAndLessThanNumberOfAlternatives() {
        int result = RandomUtils.rnd(MIN);
        assertTrue(0 <= result && result < MIN);
    }

    @RepeatedTest(10)
    public void rnd_MinMax_ReturnsNumberNotLessThanMinAndNotGreaterThanMax() {
        int result = RandomUtils.rnd(MIN, MAX);
        assertTrue(MIN <= result && result <= MAX);
    }
}
