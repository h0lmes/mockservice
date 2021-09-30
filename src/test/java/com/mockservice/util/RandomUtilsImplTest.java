package com.mockservice.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class RandomUtilsImplTest {

    private static final int MIN = 5;
    private static final int MAX = 10;

    @Test
    public void rnd_NumberOfAlternatives_ReturnsNumberNotLessThanZeroAndLessThanNumberOfAlternatives() {
        int result = new RandomUtilsImpl().rnd(MIN);
        assertTrue(0 <= result && result < MIN);
    }

    @Test
    public void rnd_MinMax_ReturnsNumberNotLessThanMinAndNotGreaterThanMax() {
        int result = new RandomUtilsImpl().rnd(MIN, MAX);
        assertTrue(MIN <= result && result <= MAX);
    }
}
