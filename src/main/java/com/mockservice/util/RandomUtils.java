package com.mockservice.util;

public interface RandomUtils {
    int rnd(int numberOfAlternatives);
    int rnd(int min, int max);
    boolean withChance(int percent);
}
