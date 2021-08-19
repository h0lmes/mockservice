package com.mockservice.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class SettingsTest {

    @Test
    public void equals() {
        Settings s1 = new Settings().setQuantum(true).setRandomAlt(true).setAlt400OnFailedRequestValidation(true);
        Settings s2 = new Settings().setQuantum(true).setRandomAlt(true).setAlt400OnFailedRequestValidation(true);
        assertEquals(s1, s2);
    }

    @Test
    public void hashCode_EqualsForEqualObjects() {
        Settings s1 = new Settings().setQuantum(true).setRandomAlt(true).setAlt400OnFailedRequestValidation(true);
        Settings s2 = new Settings().setQuantum(true).setRandomAlt(true).setAlt400OnFailedRequestValidation(true);
        assertEquals(s1.hashCode(), s2.hashCode());
    }
}
