package com.mockservice.domain;

public class Settings {

    private boolean randomAlt = false;
    private boolean quantum = false;
    private boolean failedInputValidationAlt400 = true;

    public Settings() {
        // default
    }

    public boolean getRandomAlt() {
        return randomAlt;
    }

    public boolean getQuantum() {
        return quantum;
    }

    public boolean getFailedInputValidationAlt400() {
        return failedInputValidationAlt400;
    }
}
