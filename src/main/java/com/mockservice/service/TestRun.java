package com.mockservice.service;

import com.mockservice.domain.ApiTest;

import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("java:S1149")
public class TestRun {
    private ApiTest apiTest;
    private boolean allowTrigger;
    /**
     * StringBuffer is used due to a multithreaded access
     */
    private final StringBuffer buffer = new StringBuffer();
    private int currentLine;
    private boolean running = false;
    private boolean requireStop = false;
    private final ReentrantLock lock = new ReentrantLock();

    public boolean init(ApiTest apiTest, boolean allowTrigger) {
        try {
            lock.lock();
            if (running) return false;

            this.apiTest = apiTest;
            this.allowTrigger = allowTrigger;
            buffer.setLength(0);
            buffer.trimToSize();
            currentLine = 0;
            requireStop = false;
            running = true;
            return true;
        } finally {
            lock.unlock();
        }
    }

    public boolean clear() {
        try {
            lock.lock();
            if (running) return false;

            buffer.setLength(0);
            buffer.trimToSize();
            currentLine = 0;
            requireStop = false;
            return true;
        } finally {
            lock.unlock();
        }
    }

    public ApiTest getTest() {
        return apiTest;
    }

    public boolean isAllowTrigger() {
        return allowTrigger;
    }

    public TestRun log(String value) {
        buffer.append(value);
        return this;
    }

    public TestRun log(char value) {
        buffer.append(value);
        return this;
    }

    public TestRun log(long value) {
        buffer.append(value);
        return this;
    }

    public String getLog() {
        return buffer.toString();
    }

    public boolean isEmpty() {
        return buffer.isEmpty();
    }

    public int getCurrentLine() {
        try {
            lock.lock();
            return currentLine;
        } finally {
            lock.unlock();
        }
    }

    public void setCurrentLine(int currentLine) {
        try {
            lock.lock();
            this.currentLine = currentLine;
        } finally {
            lock.unlock();
        }
    }

    public boolean isRunning() {
        try {
            lock.lock();
            return running;
        } finally {
            lock.unlock();
        }
    }

    public void setRunning(boolean running) {
        try {
            lock.lock();
            this.running = running;
        } finally {
            lock.unlock();
        }
    }

    public boolean isRequireStop() {
        try {
            lock.lock();
            return requireStop;
        } finally {
            lock.unlock();
        }
    }

    public void requestStop() {
        try {
            lock.lock();
            this.requireStop = true;
        } finally {
            lock.unlock();
        }
    }
}
