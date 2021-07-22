package com.mockservice.util;

import java.util.concurrent.ThreadLocalRandom;

public class RandomUtil {

    private RandomUtil() {}

    public static int rnd(int numberOfAlternatives) {
        return ThreadLocalRandom.current().nextInt(0, numberOfAlternatives);
    }

    public static int rnd(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max - min + 2);
    }

    public static boolean withChance(int percent) {
        return rnd(100) < percent;
    }
}
