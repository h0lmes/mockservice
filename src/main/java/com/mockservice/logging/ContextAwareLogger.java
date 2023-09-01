package com.mockservice.logging;

import net.logstash.logback.argument.StructuredArgument;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static net.logstash.logback.argument.StructuredArguments.keyValue;

public class ContextAwareLogger implements Logger {

    private static final String PARAM2 = "{}\n{}";
    private static final String PARAM3 = "{}\n{}\n{}";
    private static final String KEY_TEMP_KEYS = "__temp__keys";
    private static final String KEY_CLASS = "class";
    private static final String KEY_STACK_TRACE = "stackTrace";

    private final org.slf4j.Logger logger;
    private final String name;

    ContextAwareLogger(Class<?> clazz) {
        this.logger = org.slf4j.LoggerFactory.getLogger(clazz);
        this.name = clazz.getSimpleName();
    }

    public Logger put(@Nonnull String key, @Nullable Object obj) {
        LoggerThreadLocalMap.put(key, obj);
        return this;
    }

    public Logger clear() {
        LoggerThreadLocalMap.clear();
        return this;
    }

    public Logger with(@Nonnull String key, @Nullable Object obj) {
        LoggerThreadLocalMap.put(key, obj);

        if (LoggerThreadLocalMap.get(KEY_TEMP_KEYS) instanceof List keys) {
            keys.add(key);
        } else {
            List<String> keys = new ArrayList<>();
            keys.add(key);
            LoggerThreadLocalMap.put(KEY_TEMP_KEYS, keys);
        }

        return this;
    }

    public Logger withClass(@Nullable Class<?> clazz) {
        if (clazz != null) with(KEY_CLASS, clazz);
        return this;
    }

    public Logger withClass(@Nullable Object obj) {
        return withClass(obj == null ? null : obj.getClass());
    }

    private void clearTemp() {
        if (LoggerThreadLocalMap.get(KEY_TEMP_KEYS) instanceof List keys) {
            LoggerThreadLocalMap.remove(keys);
            LoggerThreadLocalMap.remove(KEY_TEMP_KEYS);
        }
    }

    @Override
    public Logger info(String message) {
        logger.info(getLogString(PARAM2), getArgs(message, null));
        clearTemp();
        return this;
    }

    @Override
    public Logger warn(String message, Exception e) {
        logger.warn(getLogString(PARAM3), getArgs(message, e));
        clearTemp();
        return this;
    }

    @Override
    public Logger warn(String message) {
        logger.warn(getLogString(PARAM2), getArgs(message, null));
        clearTemp();
        return this;
    }

    @Override
    public Logger error(String message, Exception e) {
        logger.error(getLogString(PARAM3), getArgs(message, e));
        clearTemp();
        return this;
    }

    @Override
    public Logger error(String message) {
        logger.error(getLogString(PARAM2), getArgs(message, null));
        clearTemp();
        return this;
    }

    @Nonnull
    private String getLogString(String initial) {
        StringBuilder sb = new StringBuilder(initial);
        var keys = LoggerThreadLocalMap.getKeys();
        if (keys != null) {
            for (String key : keys) {
                if (skipKey(key)) continue;
                sb.append("\n{}");
            }
        }
        return sb.toString();
    }

    @Nonnull
    private Object[] getArgs(String message, Exception e) {
        List<Object> args = new ArrayList<>();
        args.add(message);
        args.add(getClassArg());
        if (e != null) args.add(getStackTraceArg(e));

        var keys = LoggerThreadLocalMap.getKeys();
        if (keys != null) {
            for (String key : keys) {
                if (skipKey(key)) continue;
                args.add(getArg(key, LoggerThreadLocalMap.get(key)));
            }
        }
        return args.toArray();
    }

    private boolean skipKey(String key) {
        return KEY_CLASS.equals(key) || KEY_TEMP_KEYS.equals(key);
    }

    private StructuredArgument getArg(@Nonnull String key, @Nullable Object obj) {
        return keyValue(key, obj == null ? "null" : obj.toString());
    }

    private StructuredArgument getClassArg() {
        Class<?> clazz = (Class<?>) LoggerThreadLocalMap.get(KEY_CLASS);
        return getArg(KEY_CLASS, clazz == null ? name : clazz.getSimpleName());
    }

    private StructuredArgument getStackTraceArg(@Nonnull Exception e) {
        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            e.printStackTrace(pw);
            return getArg(KEY_STACK_TRACE, sw);
        } catch (IOException ex) {
            return getArg(KEY_STACK_TRACE, "Error reading stack trace: " + ex.getMessage());
        }
    }
}
