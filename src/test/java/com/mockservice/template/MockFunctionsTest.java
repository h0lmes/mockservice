package com.mockservice.template;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class MockFunctionsTest {

    private static final String[] EMPTY_ARGS = new String[]{};
    private static final String[] NUMERIC_ARGS = new String[]{"enum", "1", "10"};
    private static final String ENUM_VALUE1 = "value1";
    private static final String ENUM_VALUE2 = "value2";
    private static final String[] ENUM_ARGS = new String[]{"enum", ENUM_VALUE1, ENUM_VALUE2};
    private static final String REGEX_UUID =
            "\\b[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-\\b[0-9a-fA-F]{12}\\b";

    @Test
    void getFunctions_Sequence() {
        Function<String[], String> fn = MockFunctions.create().get("sequence");

        assertEquals("1", fn.apply(EMPTY_ARGS));
        assertEquals("2", fn.apply(EMPTY_ARGS));
        assertEquals("3", fn.apply(EMPTY_ARGS));
    }

    @Test
    void getFunctions_RandomInt() {
        Function<String[], String> fn = MockFunctions.create().get("random_int");

        assertDoesNotThrow(() -> Integer.parseInt(fn.apply(EMPTY_ARGS)));
    }

    @Test
    void getFunctions_RandomIntInRange() {
        Function<String[], String> fn = MockFunctions.create().get("random_int");

        assertDoesNotThrow(() -> Integer.parseInt(fn.apply(NUMERIC_ARGS)));
        int val = Integer.parseInt(fn.apply(NUMERIC_ARGS));
        assertTrue(1 <= val && val <= 10);
    }

    @Test
    void getFunctions_RandomLong() {
        Function<String[], String> fn = MockFunctions.create().get("random_long");

        assertDoesNotThrow(() -> Long.parseLong(fn.apply(EMPTY_ARGS)));
    }

    @Test
    void getFunctions_RandomLongInRange() {
        Function<String[], String> fn = MockFunctions.create().get("random_long");

        assertDoesNotThrow(() -> Long.parseLong(fn.apply(NUMERIC_ARGS)));
        long val = Long.parseLong(fn.apply(NUMERIC_ARGS));
        assertTrue(1 <= val && val <= 10);
    }

    @Test
    void getFunctions_RandomUuid() {
        Function<String[], String> fn = MockFunctions.create().get("random_uuid");

        Pattern pattern = Pattern.compile(REGEX_UUID);
        Matcher matcher = pattern.matcher(fn.apply(EMPTY_ARGS));
        assertTrue(matcher.find());
    }

    @Test
    void getFunctions_RandomString() {
        Function<String[], String> fn = MockFunctions.create().get("random_string");

        assertFalse(fn.apply(EMPTY_ARGS).isEmpty());
    }

    @Test
    void getFunctions_Enum() {
        Function<String[], String> fn = MockFunctions.create().get("enum");

        assertTrue(Set.of(ENUM_VALUE1, ENUM_VALUE2).contains(fn.apply(ENUM_ARGS)));
    }

    @Test
    void getFunctions_RandomDate() {
        Function<String[], String> fn = MockFunctions.create().get("random_date");

        assertDoesNotThrow(
                () -> LocalDate.parse(fn.apply(ENUM_ARGS),
                        DateTimeFormatter.ofPattern("uuuu-MM-dd"))
        );
    }

    @Test
    void getFunctions_RandomTimestamp() {
        Function<String[], String> fn = MockFunctions.create().get("random_timestamp");

        assertDoesNotThrow(
                () -> LocalDateTime.parse(fn.apply(ENUM_ARGS),
                        DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS"))
        );
    }

    @Test
    void getFunctions_CurrentDate() {
        Function<String[], String> fn = MockFunctions.create().get("current_date");

        assertDoesNotThrow(
                () -> LocalDate.parse(fn.apply(ENUM_ARGS),
                        DateTimeFormatter.ofPattern("uuuu-MM-dd"))
        );

        LocalDate expected = LocalDate.now();
        LocalDate date = LocalDate.parse(fn.apply(ENUM_ARGS),
                DateTimeFormatter.ofPattern("uuuu-MM-dd"));

        assertEquals(expected.getYear(), date.getYear());
        assertEquals(expected.getMonth(), date.getMonth());
        assertEquals(expected.getDayOfMonth(), date.getDayOfMonth());
    }

    @Test
    void getFunctions_CurrentTimestamp() {
        Function<String[], String> fn = MockFunctions.create().get("current_timestamp");

        assertDoesNotThrow(
                () -> LocalDateTime.parse(fn.apply(ENUM_ARGS),
                        DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS"))
        );

        LocalDateTime expected = LocalDateTime.now();
        LocalDateTime dateTime = LocalDateTime.parse(fn.apply(ENUM_ARGS),
                DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS"));
        assertEquals(expected.getYear(), dateTime.getYear());
        assertEquals(expected.getMonth(), dateTime.getMonth());
        assertEquals(expected.getDayOfMonth(), dateTime.getDayOfMonth());
    }
}
