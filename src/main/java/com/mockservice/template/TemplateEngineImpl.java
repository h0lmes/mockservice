package com.mockservice.template;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Supplier;

public class TemplateEngineImpl implements TemplateEngine {

    private final Map<String, Supplier<Function<String[], String>>> suppliers = new LinkedHashMap<>();

    public TemplateEngineImpl() {
        suppliers.put("sequence", IntSequenceFunction::new);
        suppliers.put("random_int", () -> TemplateEngineImpl::randomInt);
        suppliers.put("random_long", () -> TemplateEngineImpl::randomLong);
        suppliers.put("random_uuid", () -> TemplateEngineImpl::randomUuid);
        suppliers.put("random_string", () -> TemplateEngineImpl::randomString);
        suppliers.put("enum", () -> TemplateEngineImpl::enumFn);
        suppliers.put("random_date", () -> TemplateEngineImpl::randomDate);
        suppliers.put("random_timestamp", () -> TemplateEngineImpl::randomTimestamp);
        suppliers.put("current_date", () -> TemplateEngineImpl::currentDate);
        suppliers.put("current_timestamp", () -> TemplateEngineImpl::currentTimestamp);
    }

    @Override
    public MockFunctions getFunctions() {
        MockFunctions functions = new MockFunctions();
        suppliers.forEach((name, supplier) -> functions.put(name, supplier.get()));
        return functions;
    }

    @Override
    public boolean isFunction(String arg0) {
        return suppliers.containsKey(arg0);
    }

    private static String randomInt(String[] args) {
        int origin = intArgOrDefault(args, 1, 1);
        int bound = intArgOrDefault(args, 2, 10_000) + 1;
        if (bound <= origin) {
            bound = origin + 1;
        }
        return "" + ThreadLocalRandom.current().nextInt(origin, bound);
    }

    private static String randomLong(String[] args) {
        long origin = longArgOrDefault(args, 1, 1);
        long bound = longArgOrDefault(args, 2, 1_000_000_000_000_000L) + 1;
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
        int minLen = intArgOrDefault(args, 1, 10);
        int maxLen = intArgOrDefault(args, 2, 20) + 1;
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

    private static String enumFn(String[] args) {
        if (args.length > 1) {
            int index = ThreadLocalRandom.current().nextInt(1, args.length);
            return args[index];
        }
        return "";
    }

    @SuppressWarnings("unused")
    private static String randomDate(String[] args) {
        long startEpochDay = LocalDate.of(1970, 1, 1).toEpochDay();
        long endEpochDay = LocalDate.of(2100, 1, 1).toEpochDay();
        long randomEpochDay = ThreadLocalRandom.current().nextLong(startEpochDay, endEpochDay);
        return LocalDate.ofEpochDay(randomEpochDay)
                .format(DateTimeFormatter.ofPattern("uuuu-MM-dd"));
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
    private static String currentDate(String[] args) {
        return LocalDate.now()
                .format(DateTimeFormatter.ofPattern("uuuu-MM-dd"));
    }

    @SuppressWarnings("unused")
    private static String currentTimestamp(String[] args) {
        return ZonedDateTime
                .now(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS"));
    }

    //--------------------------------------------------------------------------
    //
    // helper functions
    //
    //--------------------------------------------------------------------------

    private static int intArgOrDefault(String[] args, int index, int def) {
        try {
            if (args.length > index) {
                return Integer.parseInt(args[index]);
            }
        } catch (Exception e) { /* ignore */ }
        return def;
    }

    private static long longArgOrDefault(String[] args, int index, long def) {
        try {
            if (args.length > index) {
                return Long.parseLong(args[index]);
            }
        } catch (Exception e) { /* ignore */ }
        return def;
    }
}
