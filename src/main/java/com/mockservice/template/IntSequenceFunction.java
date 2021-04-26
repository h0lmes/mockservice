package com.mockservice.template;

import java.util.function.Function;

public class IntSequenceFunction implements Function<String[], String> {

    private int value = 1;

    @Override
    public String apply(String[] args) {
        return "" + value++;
    }
}
