package com.mockservice.service;

import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;

public enum TemplateConstants {

    RANDOM_INT("random:int", TemplateConstants::randomInt),
    RANDOM_UUID("random:uuid", () -> UUID.randomUUID().toString()),
    RANDOM_STRING("random:string", TemplateConstants::randomString);

    private String name;
    private Supplier<String> supplier;

    TemplateConstants(String name, Supplier<String> supplier) {
        this.name = name;
        this.supplier = supplier;
    }

    public String getPlaceholder() {
        return "${" + name + "}";
    }

    public Supplier<String> getSupplier() {
        return supplier;
    }

    private static String randomInt() {
        return "" + ((int) Math.floor(1 + Math.random() * 1000));
    }

    private static String randomString() {
        return new Random().ints(97, 123)
                .limit(20)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
