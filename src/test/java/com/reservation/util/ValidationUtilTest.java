package com.reservation.util;

import static org.junit.jupiter.api.Assertions.*;

import com.reservation.service.ServiceException;
import org.junit.jupiter.api.Test;

class ValidationUtilTest {
    @Test void blankRejected() {
        assertThrows(ServiceException.class, () -> ValidationUtil.requireNonBlank("   ", "Field"));
    }
    @Test void trainNumberMustBeNumeric() throws Exception {
        assertEquals(12951, ValidationUtil.parseTrainNumber(" 12951 "));
        assertThrows(ServiceException.class, () -> ValidationUtil.parseTrainNumber("12A"));
        assertThrows(ServiceException.class, () -> ValidationUtil.parseTrainNumber("0"));
    }
    @Test void pnrFormat() throws Exception {
        assertEquals("ABC123DEF4", ValidationUtil.validatePnr(" abc123def4 "));
        assertThrows(ServiceException.class, () -> ValidationUtil.validatePnr("short"));
        assertThrows(ServiceException.class, () -> ValidationUtil.validatePnr("' OR 1=1 --"));
    }
    @Test void passwordPolicy() throws Exception {
        ValidationUtil.validatePassword("abcdefg1".toCharArray());
        assertThrows(ServiceException.class, () -> ValidationUtil.validatePassword("abcdefgh".toCharArray()));
        assertThrows(ServiceException.class, () -> ValidationUtil.validatePassword("a1".toCharArray()));
    }
    @Test void usernameRules() throws Exception {
        assertEquals("john_1", ValidationUtil.validateUsername(" john_1 "));
        assertThrows(ServiceException.class, () -> ValidationUtil.validateUsername("a b"));
    }
}
