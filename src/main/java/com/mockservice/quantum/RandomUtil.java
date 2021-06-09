package com.mockservice.quantum;

import java.util.concurrent.ThreadLocalRandom;

public class RandomUtil {

    private RandomUtil() {}

    public static int rnd(int numberOfAlternatives) {
        return ThreadLocalRandom.current().nextInt(0, numberOfAlternatives);
    }

    public static boolean withChance(int percent) {
        return rnd(100) < percent;
    }
}
