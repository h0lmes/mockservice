package com.mockservice.domain;

public class Settings {

    private boolean randomAlt = false;
    private boolean quantum = false;
    private boolean alt400OnFailedRequestValidation = true;

    public Settings() {
        /* default */
    }

    public boolean getRandomAlt() {
        return randomAlt;
    }

    public boolean getQuantum() {
        return quantum;
    }

    public boolean getAlt400OnFailedRequestValidation() {
        return alt400OnFailedRequestValidation;
    }
}
