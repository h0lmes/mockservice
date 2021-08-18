package com.mockservice.service.quantum;

import com.mockservice.util.JsonGenerator;
import com.mockservice.util.JsonUtils;
import com.mockservice.util.RandomUtils;
import com.mockservice.util.ValueGenerator;
import org.springframework.stereotype.Service;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class JsonQuantumTheory implements QuantumTheory {

    private static final String REGEX_JSON_STRING_VALUE = "\"(\\w+)\"\\s*:\\s*\"((\\\\\"|[^\"])*)\"";
    private static final String REGEX_JSON_NUMERIC_VALUE = "\"(\\w+)\"\\s*:\\s*(-?[\\d\\.e]+)";
    private static final String REGEX_JSON_BOOLEAN_VALUE = "\"(\\w+)\"\\s*:\\s*(false|true)";

    public JsonQuantumTheory() {
        /* default */
    }

    @Override
    public boolean applicable(String data) {
        return JsonUtils.isJson(data);
    }

    @Override
    public String apply(String input) {
        if (RandomUtils.withChance(20)) {
            return JsonGenerator.generate();
        }
        if (RandomUtils.withChance(10)) {
            return "";
        }
        if (RandomUtils.withChance(40)) {
            return input;
        }

        return randomizeJsonValues(input); // 43,2% chance
    }

    private static String randomizeJsonValues(String data) {
        Pattern pattern = Pattern.compile(REGEX_JSON_STRING_VALUE);
        Matcher matcher = pattern.matcher(data);
        if (matcher.find()) {
            data = matcher.replaceAll(JsonQuantumTheory::stringReplacer);
        }

        pattern = Pattern.compile(REGEX_JSON_NUMERIC_VALUE);
        matcher = pattern.matcher(data);
        if (matcher.find()) {
            data = matcher.replaceAll(JsonQuantumTheory::numberReplacer);
        }

        pattern = Pattern.compile(REGEX_JSON_BOOLEAN_VALUE);
        matcher = pattern.matcher(data);
        if (matcher.find()) {
            data = matcher.replaceAll(JsonQuantumTheory::booleanReplacer);
        }

        return data;
    }

    private static String stringReplacer(MatchResult matchResult) {
        String name = matchResult.group(1);
        if (RandomUtils.withChance(10)) {
            return "\"" + name + "\": null";
        }
        return "\"" + name + "\": \"" + ValueGenerator.randomString() + "\"";
    }

    private static String numberReplacer(MatchResult matchResult) {
        String name = matchResult.group(1);
        if (RandomUtils.withChance(10)) {
            return "\"" + name + "\": null";
        }
        return "\"" + name + "\": " + ValueGenerator.randomNumberString();
    }

    private static String booleanReplacer(MatchResult matchResult) {
        String name = matchResult.group(1);
        return "\"" + name + "\": " + ValueGenerator.randomBooleanString();
    }
}
