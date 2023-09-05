package com.mockservice.logging;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith(OutputCaptureExtension.class)
class ContextAwareLoggerTest {

    private static final String KEY_1 = "1";
    private static final String KEY_2 = "2";
    private static final String KEY_3 = "3";
    private static final String VAL_1 = "v1";
    private static final String VAL_2 = "v2";
    private static final String VAL_3 = "v3";
    private static final String MESSAGE_TEXT = "message text";
    private static final String EXCEPTION_TEXT = "exception text";

    private String kv(String key, String value) {
        return key.concat("=").concat(value);
    }

    @Test
    void testInfo(CapturedOutput out) {
        var log = new ContextAwareLogger(Object.class);
        log
                .with(KEY_1, VAL_1)
                .with(KEY_2, VAL_2)
                .with(KEY_3, VAL_3)
                .info(MESSAGE_TEXT);

        Assertions.assertTrue(out.getOut().contains(kv(KEY_1, VAL_1)));
        Assertions.assertTrue(out.getOut().contains(kv(KEY_2, VAL_2)));
        Assertions.assertTrue(out.getOut().contains(kv(KEY_3, VAL_3)));
        Assertions.assertTrue(out.getOut().contains(MESSAGE_TEXT));
    }

    @Test
    void testInfoWithClassOverride(CapturedOutput out) {
        var log = new ContextAwareLogger(Object.class);
        var clazz = Exception.class;
        log
                .withClass(clazz)
                .with(KEY_2, VAL_2)
                .with(KEY_3, VAL_3)
                .info(MESSAGE_TEXT);

        Assertions.assertTrue(out.getOut().contains("class=" + clazz.getSimpleName()));
        Assertions.assertTrue(out.getOut().contains(kv(KEY_2, VAL_2)));
        Assertions.assertTrue(out.getOut().contains(kv(KEY_3, VAL_3)));
        Assertions.assertTrue(out.getOut().contains(MESSAGE_TEXT));
    }

    @Test
    void testInfoWithObjectOverride(CapturedOutput out) {
        var log = new ContextAwareLogger(Object.class);
        log
                .withClass(new Exception())
                .with(KEY_2, VAL_2)
                .with(KEY_3, VAL_3)
                .info(MESSAGE_TEXT);

        Assertions.assertTrue(out.getOut().contains("class=" + Exception.class.getSimpleName()));
        Assertions.assertTrue(out.getOut().contains(kv(KEY_2, VAL_2)));
        Assertions.assertTrue(out.getOut().contains(kv(KEY_3, VAL_3)));
        Assertions.assertTrue(out.getOut().contains(MESSAGE_TEXT));
    }

    @Test
    void testInfoWithNullClassOverride(CapturedOutput out) {
        var log = new ContextAwareLogger(Object.class);
        log
                .withClass(null)
                .with(KEY_2, VAL_2)
                .with(KEY_3, VAL_3)
                .info(MESSAGE_TEXT);

        Assertions.assertTrue(out.getOut().contains("class=" + Object.class.getSimpleName()));
        Assertions.assertTrue(out.getOut().contains(kv(KEY_2, VAL_2)));
        Assertions.assertTrue(out.getOut().contains(kv(KEY_3, VAL_3)));
        Assertions.assertTrue(out.getOut().contains(MESSAGE_TEXT));
    }

    @Test
    void testWarn(CapturedOutput out) {
        var log = new ContextAwareLogger(Object.class);
        log
                .with(KEY_1, VAL_1)
                .with(KEY_2, VAL_2)
                .with(KEY_3, VAL_3)
                .warn(MESSAGE_TEXT);

        Assertions.assertTrue(out.getOut().contains(kv(KEY_1, VAL_1)));
        Assertions.assertTrue(out.getOut().contains(kv(KEY_2, VAL_2)));
        Assertions.assertTrue(out.getOut().contains(kv(KEY_3, VAL_3)));
        Assertions.assertTrue(out.getOut().contains(MESSAGE_TEXT));
    }

    @Test
    void testWarnWithException(CapturedOutput out) {
        var log = new ContextAwareLogger(Object.class);
        log
                .with(KEY_1, VAL_1)
                .with(KEY_2, VAL_2)
                .with(KEY_3, VAL_3)
                .warn(MESSAGE_TEXT, new Exception(EXCEPTION_TEXT));

        Assertions.assertTrue(out.getOut().contains(kv(KEY_1, VAL_1)));
        Assertions.assertTrue(out.getOut().contains(kv(KEY_2, VAL_2)));
        Assertions.assertTrue(out.getOut().contains(kv(KEY_3, VAL_3)));
        Assertions.assertTrue(out.getOut().contains(MESSAGE_TEXT));
        Assertions.assertTrue(out.getOut().contains(EXCEPTION_TEXT));
    }

    @Test
    void testError(CapturedOutput out) {
        var log = new ContextAwareLogger(Object.class);
        log
                .with(KEY_1, VAL_1)
                .with(KEY_2, VAL_2)
                .with(KEY_3, VAL_3)
                .error(MESSAGE_TEXT);

        Assertions.assertTrue(out.getOut().contains(kv(KEY_1, VAL_1)));
        Assertions.assertTrue(out.getOut().contains(kv(KEY_2, VAL_2)));
        Assertions.assertTrue(out.getOut().contains(kv(KEY_3, VAL_3)));
        Assertions.assertTrue(out.getOut().contains(MESSAGE_TEXT));
    }

    @Test
    void testErrorWithException(CapturedOutput out) {
        var log = new ContextAwareLogger(Object.class);
        log
                .with(KEY_1, VAL_1)
                .with(KEY_2, VAL_2)
                .with(KEY_3, VAL_3)
                .error(MESSAGE_TEXT, new Exception(EXCEPTION_TEXT));

        Assertions.assertTrue(out.getOut().contains(kv(KEY_1, VAL_1)));
        Assertions.assertTrue(out.getOut().contains(kv(KEY_2, VAL_2)));
        Assertions.assertTrue(out.getOut().contains(kv(KEY_3, VAL_3)));
        Assertions.assertTrue(out.getOut().contains(MESSAGE_TEXT));
        Assertions.assertTrue(out.getOut().contains(EXCEPTION_TEXT));
    }
}
