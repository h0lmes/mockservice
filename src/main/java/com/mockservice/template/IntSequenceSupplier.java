package com.mockservice.template;

import java.util.function.Supplier;

public class IntSequenceSupplier implements Supplier<String> {

    private int value = 1;

    @Override
    public String get() {
        return "" + value++;
    }
}
