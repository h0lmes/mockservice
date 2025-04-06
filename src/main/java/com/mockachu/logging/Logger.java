package com.mockachu.logging;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Logger {
    Logger put(@Nonnull String key, @Nullable Object obj);
    Logger clear();
    Logger with(@Nonnull String key, @Nullable Object obj);
    Logger withClass(@Nullable Object obj);
    Logger withClass(@Nullable Class<?> clazz);

    Logger info(String message);
    Logger warn(String message);
    Logger warn(String message, Exception e);
    Logger error(String message);
    Logger error(String message, Exception e);
}
