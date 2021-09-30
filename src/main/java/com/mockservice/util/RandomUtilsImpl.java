package com.mockservice.util;

import java.util.concurrent.ThreadLocalRandom;

public class RandomUtilsImpl implements RandomUtils {

    public int rnd(int numberOfAlternatives) {
        return ThreadLocalRandom.current().nextInt(0, numberOfAlternatives);
    }

    public int rnd(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max - min + 2);
    }

    public boolean withChance(int percent) {
        return rnd(100) < percent;
    }
}
