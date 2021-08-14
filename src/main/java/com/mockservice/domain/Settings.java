package com.mockservice.domain;

import java.util.Objects;

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

    public Settings setRandomAlt(boolean randomAlt) {
        this.randomAlt = randomAlt;
        return this;
    }

    public boolean getQuantum() {
        return quantum;
    }

    public Settings setQuantum(boolean quantum) {
        this.quantum = quantum;
        return this;
    }

    public boolean getAlt400OnFailedRequestValidation() {
        return alt400OnFailedRequestValidation;
    }

    public Settings setAlt400OnFailedRequestValidation(boolean alt400OnFailedRequestValidation) {
        this.alt400OnFailedRequestValidation = alt400OnFailedRequestValidation;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(randomAlt, quantum, alt400OnFailedRequestValidation);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Settings)) return false;
        Settings other = (Settings) o;
        return randomAlt == other.getRandomAlt()
                && quantum == other.getQuantum()
                && alt400OnFailedRequestValidation == other.getAlt400OnFailedRequestValidation();
    }
}
