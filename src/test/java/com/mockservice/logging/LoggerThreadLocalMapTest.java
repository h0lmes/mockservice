package com.mockservice.logging;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LoggerThreadLocalMapTest {

    @Test
    void testPutGetRemove() {
        LoggerThreadLocalMap.put("key", "value");
        Assertions.assertEquals("value", LoggerThreadLocalMap.get("key"));

        LoggerThreadLocalMap.remove("key");
        Assertions.assertNull(LoggerThreadLocalMap.get("key"));
    }

    @Test
    void testPutGetClear() {
        LoggerThreadLocalMap.put("key", "value");
        Assertions.assertEquals("value", LoggerThreadLocalMap.get("key"));

        LoggerThreadLocalMap.clear();
        Assertions.assertNull(LoggerThreadLocalMap.get("key"));
    }
}
