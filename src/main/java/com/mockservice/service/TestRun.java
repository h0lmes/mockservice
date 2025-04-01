package com.mockservice.service;

import com.mockservice.domain.ApiTest;
import com.mockservice.template.MockVariables;
import com.mockservice.util.IOUtils;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("java:S1149")
public class TestRun {
    private ApiTest apiTest;
    private boolean allowTrigger;
    private List<String> lines;
    /**
     * StringBuffer is used due to a multithreaded access
     */
    private final StringBuffer buffer = new StringBuffer();
    private Instant startedAt;
    private int currentLine;
    private int step;
    private boolean running = false;
    private boolean requestStop = false;
    private MockVariables mockVariables;
    private TestRunErrorLevel errorLevel;
    private final ReentrantLock lock = new ReentrantLock();

    public boolean init(ApiTest apiTest, boolean allowTrigger) {
        try {
            lock.lock();
            if (running) return false;

            this.apiTest = apiTest;
            this.allowTrigger = allowTrigger;
            this.lines = IOUtils.toList(apiTest.getPlan());
            buffer.setLength(0);
            buffer.trimToSize();
            currentLine = 0;
            step = 1;
            requestStop = false;
            startedAt = Instant.now();
            mockVariables = null;
            errorLevel = TestRunErrorLevel.SUCCESS;
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
            step = 1;
            mockVariables = null;
            requestStop = false;
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

    public boolean hasLine() {
        return currentLine < lines.size();
    }

    public String getLine() {
        return lines.get(currentLine);
    }

    public void nextLine() {
        try {
            lock.lock();
            this.currentLine++;
            this.step++;
            while (hasLine() && lines.get(currentLine).trim().isEmpty()) this.currentLine++;
        } finally {
            lock.unlock();
        }
    }

    public int getStep() {
        return step;
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

    public boolean isRequestStop() {
        try {
            lock.lock();
            return requestStop;
        } finally {
            lock.unlock();
        }
    }

    public void requestStop() {
        try {
            lock.lock();
            this.requestStop = true;
        } finally {
            lock.unlock();
        }
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public long getDurationMillis() {
        return Instant.now().toEpochMilli() - startedAt.toEpochMilli();
    }

    public MockVariables getMockVariables() {
        return mockVariables;
    }

    public void setMockVariables(MockVariables mockVariables) {
        this.mockVariables = mockVariables;
    }

    public TestRunErrorLevel getErrorLevel() {
        return errorLevel;
    }

    public void setErrorLevel(TestRunErrorLevel errorLevel) {
        this.errorLevel = this.errorLevel.compareTo(errorLevel) < 0 ? errorLevel : this.errorLevel;
    }

    public boolean isFailed() {
        return TestRunErrorLevel.FAILED.equals(getErrorLevel());
    }
}
