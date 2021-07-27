package com.mockservice.service;

import com.mockservice.util.RandomUtils;

import java.util.concurrent.ThreadLocalRandom;

public interface QuantumTheory {
    int[] statusCodes = {
            200, 201, 202, 203, 204, 205,
            300, 301, 302, 303, 304, 305, 306, 307, 308,
            400, 401, 402, 403, 404, 405, 406, 407, 408, 409, 410, 411, 412, 413, 414, 415, 416, 417, 418, 421, 422, 423, 424, 425, 426, 428, 429, 431, 451,
            500, 501, 502, 503, 504, 505, 506, 507, 508, 509, 510, 511
    };

    default int randomStatusCode() {
        return statusCodes[RandomUtils.rnd(statusCodes.length)];
    }

    default int apply(int statusCode) {
        if (RandomUtils.withChance(20)) {
             return randomStatusCode();
        }
        return statusCode;
    }

    default void delay() {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(10, 3000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    String apply(String data);
}
