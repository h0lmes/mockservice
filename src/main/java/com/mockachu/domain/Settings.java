package com.mockachu.domain;

public class Settings {

    private boolean randomAlt = false;
    private boolean quantum = false;
    private boolean alt400OnFailedRequestValidation = true;
    private String certificate;

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

    public String getCertificate() {
        return certificate;
    }

    public Settings setCertificate(String certificate) {
        this.certificate = certificate;
        return this;
    }
}
