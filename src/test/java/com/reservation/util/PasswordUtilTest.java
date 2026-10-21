package com.reservation.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PasswordUtilTest {
    @Test void hashVerifiesAndIsSalted() {
        char[] pw = "Secret123".toCharArray();
        String h1 = PasswordUtil.hash(pw), h2 = PasswordUtil.hash(pw);
        assertNotEquals(h1, h2);
        assertTrue(PasswordUtil.verify(pw, h1));
        assertFalse(PasswordUtil.verify("Wrong123".toCharArray(), h1));
        assertFalse(PasswordUtil.verify(pw, "garbage"));
    }
}
