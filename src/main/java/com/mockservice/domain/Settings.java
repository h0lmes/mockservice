package com.mockservice.domain;

public class Settings {

    private boolean randomSuffix = false;
    private boolean quantum = false;

    public Settings() {
        // default
    }

    public boolean getRandomSuffix() {
        return randomSuffix;
    }

    public boolean getQuantum() {
        return quantum;
    }
}
