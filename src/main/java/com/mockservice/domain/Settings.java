package com.mockservice.domain;

public class Settings {

    private boolean randomAlt = false;
    private boolean quantum = false;

    public Settings() {
        // default
    }

    public boolean getRandomAlt() {
        return randomAlt;
    }

    public boolean getQuantum() {
        return quantum;
    }
}
