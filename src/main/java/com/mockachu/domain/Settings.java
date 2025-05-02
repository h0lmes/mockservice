package com.mockachu.domain;

public class Settings {

    private boolean randomAlt = false;
    private boolean quantum = false;
    private boolean alt400OnFailedRequestValidation = true;
    private String certificate;
    private String initialContext;
    private boolean proxyEnabled = false;
    private String proxyLocation;

    public Settings() {
        /* default */
    }

    public boolean isRandomAlt() {
        return randomAlt;
    }

    public Settings setRandomAlt(boolean randomAlt) {
        this.randomAlt = randomAlt;
        return this;
    }

    public boolean isQuantum() {
        return quantum;
    }

    public Settings setQuantum(boolean quantum) {
        this.quantum = quantum;
        return this;
    }

    public boolean isAlt400OnFailedRequestValidation() {
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

    public String getInitialContext() {
        return initialContext;
    }

    public Settings setInitialContext(String initialContext) {
        this.initialContext = initialContext;
        return this;
    }

    public boolean isProxyEnabled() {
        return proxyEnabled;
    }

    public Settings setProxyEnabled(boolean proxyEnabled) {
        this.proxyEnabled = proxyEnabled;
        return this;
    }

    public String getProxyLocation() {
        return proxyLocation;
    }

    public Settings setProxyLocation(String proxyLocation) {
        this.proxyLocation = proxyLocation;
        return this;
    }
}
