package com.mockachu.logging;

public class ContextAwareLoggerFactory {

    private ContextAwareLoggerFactory() {
        // private
    }

    public static ContextAwareLogger getLogger(Class<?> clazz) {
        return new ContextAwareLogger(clazz);
    }
}
