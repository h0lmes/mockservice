package com.mockservice.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class QuantumJsonTheory implements QuantumTheory {

    private static final String QUERTY = "ABCDEF GHIJKLMN OPQRST UVWXYZ abcdef ghijklmn opqrst uvwxyz ,.!-";
    private static final char[] chars = QUERTY.toCharArray();
    private static final String[] booleans = {"false", "true"};
    private static final int MIN_DELAY = 10;
    private static final int MAX_DELAY = 3000;
    private static final String REGEX_JSON_STRING_VALUE = "\"(\\w+)\"\\s*:\\s*\"((\\\\\"|[^\"])*)\"";
    private static final String REGEX_JSON_NUMERIC_VALUE = "\"(\\w+)\"\\s*:\\s*(-?[\\d\\.e]+)";
    private static final String REGEX_JSON_BOOLEAN_VALUE = "\"(\\w+)\"\\s*:\\s*(false|true)";

    public QuantumJsonTheory() {
        // default
    }

    @Override
    public String apply(String data) {
        if (chanceIs(40)) {
            return data;
        }

        Pattern pattern = Pattern.compile(REGEX_JSON_STRING_VALUE);
        Matcher matcher = pattern.matcher(data);
        if (matcher.find()) {
            data = matcher.replaceAll(this::stringReplacer);
        }

        pattern = Pattern.compile(REGEX_JSON_NUMERIC_VALUE);
        matcher = pattern.matcher(data);
        if (matcher.find()) {
            data = matcher.replaceAll(this::numberReplacer);
        }

        pattern = Pattern.compile(REGEX_JSON_BOOLEAN_VALUE);
        matcher = pattern.matcher(data);
        if (matcher.find()) {
            data = matcher.replaceAll(this::booleanReplacer);
        }

        return data;
    }

    private String stringReplacer(MatchResult matchResult) {
        String name = matchResult.groupCount() > 0 ? matchResult.group(1) : randomString();
        return "\"" + name + "\": " + randomString();
    }

    private static String randomString() {
        if (chanceIs(10)) {
            return "null";
        }

        int len = ThreadLocalRandom.current().nextInt(1, 31);
        String str = ThreadLocalRandom.current()
                .ints(0, chars.length)
                .limit(len)
                .map(i -> chars[i])
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        return "\"" + str + "\"";
    }

    private String numberReplacer(MatchResult matchResult) {
        String name = matchResult.groupCount() > 0 ? matchResult.group(1) : randomString();
        return "\"" + name + "\": " + randomNumber();
    }

    private static String randomNumber() {
        if (chanceIs(10)) {
            return "null";
        }

        int len = ThreadLocalRandom.current().nextInt(1, 11);
        String number = ThreadLocalRandom.current()
                .ints(0, 10)
                .limit(len)
                .boxed()
                .map(String::valueOf)
                .collect(Collectors.joining());
        number = maybeFloatify(number);
        return number;
    }

    private static String maybeFloatify(String number) {
        boolean canFloatify = number.length() >= 3;
        if (canFloatify && chanceIs(50)) {
            int pointPosition = ThreadLocalRandom.current().nextInt(1, number.length() - 1);
            number = number.substring(0, pointPosition) + "." + number.substring(pointPosition + 1);
        }
        return number;
    }

    private String booleanReplacer(MatchResult matchResult) {
        String name = matchResult.groupCount() > 0 ? matchResult.group(1) : randomString();
        return "\"" + name + "\": " + randomBoolean();
    }

    private static String randomBoolean() {
        return booleans[rnd(2)];
    }

    @Override
    public void delay() {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(MIN_DELAY, MAX_DELAY));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static int rnd(int numberOfAlternatives) {
        return ThreadLocalRandom.current().nextInt(0, numberOfAlternatives);
    }

    private static boolean chanceIs(int percent) {
        return rnd(100) < percent;
    }
}
