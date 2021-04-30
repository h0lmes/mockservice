package com.mockservice.template;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Supplier;

public enum TemplateFunction {

    SEQUENCE_INT("sequence", IntSequenceFunction::new),
    RANDOM_INT("random_int", () -> TemplateFunction::randomInt),
    RANDOM_LONG("random_long", () -> TemplateFunction::randomLong),
    RANDOM_UUID("random_uuid", () -> TemplateFunction::randomUuid),
    RANDOM_STRING("random_string", () -> TemplateFunction::randomString),
    RANDOM_STRINGS("random_string_of", () -> TemplateFunction::randomStringOf),
    RANDOM_DATE("random_date", () -> TemplateFunction::randomDate),
    RANDOM_TIMESTAMP("random_timestamp", () -> TemplateFunction::randomTimestamp),
    CURRENT_DATE("current_date", () -> TemplateFunction::currentDate),
    CURRENT_TIMESTAMP("current_timestamp", () -> TemplateFunction::currentTimestamp);

    private String name;
    private Supplier<Function<String[], String>> supplier;

    TemplateFunction(String name, Supplier<Function<String[], String>> supplier) {
        this.name = name;
        this.supplier = supplier;
    }

    public String getName() {
        return name;
    }

    public Function<String[], String> getFunction() {
        return supplier.get();
    }

    // helper function
    private static int argumentInt(String[] args, int index, int def) {
        try {
            if (args.length > index) {
                return Integer.parseInt(args[index]);
            }
        } catch (Exception e) {
            // ignore
        }
        return def;
    }

    // helper function
    private static long argumentLong(String[] args, int index, long def) {
        try {
            if (args.length > index) {
                return Long.parseLong(args[index]);
            }
        } catch (Exception e) {
            // ignore
        }
        return def;
    }

    private static String randomInt(String[] args) {
        int origin = argumentInt(args,1, 1);
        int bound = argumentInt(args,2, 10000) + 1;
        if (bound <= origin) {
            bound = origin + 1;
        }
        return "" + ThreadLocalRandom.current().nextInt(origin, bound);
    }

    private static String randomLong(String[] args) {
        long origin = argumentLong(args,1, 1);
        long bound = argumentLong(args,2, 1_000_000_000_000_000L) + 1;
        if (bound <= origin) {
            bound = origin + 1;
        }
        return "" + ThreadLocalRandom.current().nextLong(origin, bound);
    }

    @SuppressWarnings("unused")
    private static String randomUuid(String[] args) {
        return UUID.randomUUID().toString();
    }

    private static String randomString(String[] args) {
        int minLen = argumentInt(args,1, 10);
        int maxLen = argumentInt(args,2, 20) + 1;
        if (maxLen <= minLen) {
            maxLen = minLen + 1;
        }
        int len = ThreadLocalRandom.current().nextInt(minLen, maxLen);

        return ThreadLocalRandom.current()
                .ints(97, 123)
                .limit(len)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    private static String randomStringOf(String[] args) {
        if (args.length > 1) {
            int index = ThreadLocalRandom.current().nextInt(1, args.length);
            return args[index];
        }
        return "";
    }

    @SuppressWarnings("unused")
    public static String randomDate(String[] args) {
        long startEpochDay = LocalDate.of(1970, 1, 1).toEpochDay();
        long endEpochDay = LocalDate.of(2100, 1, 1).toEpochDay();
        long randomEpochDay = ThreadLocalRandom.current().nextLong(startEpochDay, endEpochDay);
        return LocalDate.ofEpochDay(randomEpochDay).toString();
    }

    @SuppressWarnings("unused")
    private static String randomTimestamp(String[] args) {
        long startEpochDay = LocalDate.of(1970, 1, 1).toEpochDay();
        long endEpochDay = LocalDate.of(2100, 1, 1).toEpochDay();
        long randomEpochDay = ThreadLocalRandom.current().nextLong(startEpochDay, endEpochDay);
        long randomSecondOfDay = ThreadLocalRandom.current().nextLong(0L, 86400L);
        return ZonedDateTime
                .of(LocalDate.ofEpochDay(randomEpochDay), LocalTime.ofSecondOfDay(randomSecondOfDay), ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS"));
    }

    @SuppressWarnings("unused")
    public static String currentDate(String[] args) {
        return LocalDate.now().toString();
    }

    @SuppressWarnings("unused")
    public static String currentTimestamp(String[] args) {
        return ZonedDateTime
                .now(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS"));
    }
}
