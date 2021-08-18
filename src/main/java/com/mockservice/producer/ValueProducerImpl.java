package com.mockservice.producer;

import com.mockservice.util.RandomUtils;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class ValueProducerImpl implements ValueProducer {

    private static final String QUERTY = "ABCDEF GHIJKLMN OPQRST UVWXYZ abcdef ghijklmn opqrst uvwxyz ,.!-";
    private static final char[] CHARS = QUERTY.toCharArray();
    private static final String ARTICLE = "Eight decades have passed since physicists realized that the theories of quantum mechanics and gravity don’t fit together, and the puzzle of how to combine the two remains unsolved. In the last few decades, researchers have pursued the problem in two separate programs — string theory and loop quantum gravity — that are widely considered incompatible by their practitioners. But now some scientists argue that joining forces is the way forward.\n" +
            "Among the attempts to unify quantum theory and gravity, string theory has attracted the most attention. Its premise is simple: Everything is made of tiny strings. The strings may be closed unto themselves or have loose ends; they can vibrate, stretch, join or split. And in these manifold appearances lie the explanations for all phenomena we observe, both matter and space-time included.\n" +
            "Loop quantum gravity, by contrast, is concerned less with the matter that inhabits space-time than with the quantum properties of space-time itself. In loop quantum gravity, or LQG, space-time is a network. The smooth background of Einstein’s theory of gravity is replaced by nodes and links to which quantum properties are assigned. In this way, space is built up of discrete chunks. LQG is in large part a study of these chunks.\n" +
            "This approach has long been thought incompatible with string theory. Indeed, the conceptual differences are obvious and profound. For starters, LQG studies bits of space-time, whereas string theory investigates the behavior of objects within space-time. Specific technical problems separate the fields. String theory requires that space-time have 10 dimensions; LQG doesn’t work in higher dimensions. String theory also implies the existence of supersymmetry, in which all known particles have yet-undiscovered partners. Supersymmetry isn’t a feature of LQG.";
    private final String[] vocabulary;
    private static final int[] stringLengths = {1, 1, 1, 2, 2, 3};
    private static final String[] booleans = {"false", "true"};

    public ValueProducerImpl() {
        vocabulary = ARTICLE
                .replaceAll("[^A-Za-z\\-\\s]", "")
                .toLowerCase()
                .split("\\s+");
    }

    @Override
    public String randomString() {
        if (RandomUtils.withChance(20)) {
            return randomChars();
        }
        return randomWords(stringLengths[RandomUtils.rnd(stringLengths.length)]);
    }

    @Override
    public String randomWords(int numberOfWords) {
        return ThreadLocalRandom.current()
                .ints(0, vocabulary.length)
                .limit(numberOfWords)
                .boxed()
                .map(i -> vocabulary[i])
                .collect(Collectors.joining(" "));
    }

    private static String randomChars() {
        int len = ThreadLocalRandom.current().nextInt(1, 31);
        return ThreadLocalRandom.current()
                .ints(0, CHARS.length)
                .limit(len)
                .map(i -> CHARS[i])
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    @Override
    public String randomNumberString() {
        return maybeFloatify(randomIntegerString());
    }

    @Override
    public String randomIntegerString() {
        int len = RandomUtils.rnd(8) + 1;
        String number = ThreadLocalRandom.current()
                .ints(0, 9)
                .limit(len)
                .boxed()
                .map(String::valueOf)
                .collect(Collectors.joining());
        return stripLeadingZeroes(number);
    }

    private static String stripLeadingZeroes(String str) {
        return str.replaceFirst("^0+(?!$)", "");
    }

    private static String maybeFloatify(String number) {
        if (RandomUtils.withChance(75)) {
            if (number.length() >= 2) {
                int pointPosition = ThreadLocalRandom.current().nextInt(1, number.length());
                number = number.substring(0, pointPosition) + "." + number.substring(pointPosition);
            } else {
                number = "0." + number;
            }
        }
        return number;
    }

    @Override
    public String randomBooleanString() {
        return booleans[RandomUtils.rnd(2)];
    }
}
