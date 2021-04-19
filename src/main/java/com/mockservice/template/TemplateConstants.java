package com.mockservice.template;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public enum TemplateConstants {

    SEQUENCE_INT("sequence:int", IntSequenceSupplier::new),
    RANDOM_INT("random:int", () -> TemplateConstants::randomInt),
    RANDOM_UUID("random:uuid", () -> TemplateConstants::randomUuid),
    RANDOM_STRING("random:string", () -> TemplateConstants::randomString),
    RANDOM_DATE("random:date", () -> TemplateConstants::randomDate),
    RANDOM_TIMESTAMP("random:timestamp", () -> TemplateConstants::randomTimestamp),
    CURRENT_DATE("current:date", () -> TemplateConstants::currentDate),
    CURRENT_TIMESTAMP("current:timestamp", () -> TemplateConstants::currentTimestamp);

    private String name;
    private Supplier<Supplier<String>> supplier;

    TemplateConstants(String name, Supplier<Supplier<String>> supplier) {
        this.name = name;
        this.supplier = supplier;
    }

    public String getName() {
        return name;
    }

    public Supplier<String> getSupplier() {
        return supplier.get();
    }

    private static String randomInt() {
        return "" + ThreadLocalRandom.current().nextInt(1, 10000);
    }

    private static String randomUuid() {
        return UUID.randomUUID().toString();
    }

    private static String randomString() {
        return ThreadLocalRandom.current()
                .ints(97, 123)
                .limit(20)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public static String randomDate() {
        long startEpochDay = LocalDate.of(1970, 1, 1).toEpochDay();
        long endEpochDay = LocalDate.of(2100, 1, 1).toEpochDay();
        long randomEpochDay = ThreadLocalRandom.current().nextLong(startEpochDay, endEpochDay);
        return LocalDate.ofEpochDay(randomEpochDay).toString();
    }

    private static String randomTimestamp() {
        long startEpochDay = LocalDate.of(1970, 1, 1).toEpochDay();
        long endEpochDay = LocalDate.of(2100, 1, 1).toEpochDay();
        long randomEpochDay = ThreadLocalRandom.current().nextLong(startEpochDay, endEpochDay);
        long randomSecondOfDay = ThreadLocalRandom.current().nextLong(0, 24 * 60 * 60);
        return ZonedDateTime
                .of(LocalDate.ofEpochDay(randomEpochDay), LocalTime.ofSecondOfDay(randomSecondOfDay), ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS"));
    }

    public static String currentDate() {
        return LocalDate.now().toString();
    }

    public static String currentTimestamp() {
        return ZonedDateTime
                .now(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS"));
    }
}
