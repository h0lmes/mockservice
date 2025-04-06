package com.mockachu.components;

import com.mockachu.util.JsonUtils;
import com.mockachu.util.RandomUtils;
import org.springframework.stereotype.Service;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class JsonQuantumTheory implements QuantumTheory {

    int[] statusCodes = {
            200, 201, 202, 203, 204, 205,
            300, 301, 302, 303, 304, 305, 306, 307, 308,
            400, 401, 402, 403, 404, 405, 406, 407, 408, 409, 410,
            411, 412, 413, 414, 415, 416, 417, 418, 421, 422, 423,
            424, 425, 426, 428, 429, 431, 451,
            500, 501, 502, 503, 504, 505, 506, 507, 508, 509, 510, 511
    };

    private static final String REGEX_JSON_STRING_VALUE = "\"(\\w+)\"\\s*:\\s*\"((\\\\\"|[^\"])*)\"";
    private static final String REGEX_JSON_NUMERIC_VALUE = "\"(\\w+)\"\\s*:\\s*(-?[\\d\\.e]+)";
    private static final String REGEX_JSON_BOOLEAN_VALUE = "\"(\\w+)\"\\s*:\\s*(false|true)";

    private final ValueProducer valueProducer;
    private final JsonProducer jsonProducer;
    private final RandomUtils randomUtils;

    public JsonQuantumTheory(ValueProducer valueProducer,
                             JsonProducer jsonProducer,
                             RandomUtils randomUtils) {
        this.valueProducer = valueProducer;
        this.jsonProducer = jsonProducer;
        this.randomUtils = randomUtils;
    }

    @Override
    public int apply(int statusCode) {
        if (randomUtils.withChance(20)) {
            return randomStatusCode();
        }
        return statusCode;
    }

    private int randomStatusCode() {
        return statusCodes[randomUtils.rnd(statusCodes.length)];
    }

    @Override
    public void delay() {
        try {
            Thread.sleep(randomUtils.rnd(10, 3000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public boolean applicable(String data) {
        return JsonUtils.isJson(data);
    }

    @Override
    public String apply(String input) {
        if (randomUtils.withChance(20)) {
            return jsonProducer.generate();
        }
        if (randomUtils.withChance(10)) {
            return "";
        }
        if (randomUtils.withChance(40)) {
            return input;
        }

        return randomizeJsonValues(input); // 43,2% chance
    }

    private String randomizeJsonValues(String data) {
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
        String name = matchResult.group(1);
        if (randomUtils.withChance(10)) {
            return "\"" + name + "\": null";
        }
        return "\"" + name + "\": \"" + valueProducer.randomString() + "\"";
    }

    private String numberReplacer(MatchResult matchResult) {
        String name = matchResult.group(1);
        if (randomUtils.withChance(10)) {
            return "\"" + name + "\": null";
        }
        return "\"" + name + "\": " + valueProducer.randomNumberString();
    }

    private String booleanReplacer(MatchResult matchResult) {
        String name = matchResult.group(1);
        return "\"" + name + "\": " + valueProducer.randomBooleanString();
    }
}
