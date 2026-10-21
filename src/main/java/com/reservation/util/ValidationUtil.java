package com.reservation.util;

import com.reservation.service.ServiceException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class ValidationUtil {
    public static final List<String> CLASS_TYPES = List.of(
            "Sleeper (SL)", "AC 3 Tier (3A)", "AC 2 Tier (2A)", "AC First Class (1A)", "Chair Car (CC)");

    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9_]{3,30}$");
    private static final Pattern PERSON_NAME = Pattern.compile("^\\p{L}[\\p{L} .'-]{0,99}$");
    private static final Pattern PNR = Pattern.compile("^[A-Z0-9]{10}$");

    private ValidationUtil() { }

    /** Trims and collapses internal whitespace; null becomes "". */
    public static String clean(String s) {
        return s == null ? "" : s.trim().replaceAll("\\s+", " ");
    }

    public static String requireNonBlank(String value, String label) throws ServiceException {
        String v = clean(value);
        if (v.isEmpty()) throw new ServiceException(label + " is required.");
        return v;
    }

    public static String validateUsername(String value) throws ServiceException {
        String v = requireNonBlank(value, "Username");
        if (!USERNAME.matcher(v).matches())
            throw new ServiceException("Username must be 3-30 characters: letters, digits or underscore.");
        return v;
    }

    public static void validatePassword(char[] pw) throws ServiceException {
        if (pw == null || pw.length == 0) throw new ServiceException("Password is required.");
        boolean letter = false, digit = false;
        for (char c : pw) {
            if (Character.isLetter(c)) letter = true;
            if (Character.isDigit(c)) digit = true;
        }
        if (pw.length < 8 || pw.length > 128 || !letter || !digit)
            throw new ServiceException("Password must be 8-128 characters and contain at least one letter and one digit.");
    }

    public static String validatePersonName(String value, String label) throws ServiceException {
        String v = requireNonBlank(value, label);
        if (!PERSON_NAME.matcher(v).matches())
            throw new ServiceException(label + " may contain only letters, spaces, . ' - (max 100 characters).");
        return v;
    }

    public static String validatePnr(String value) throws ServiceException {
        String v = requireNonBlank(value, "PNR").toUpperCase(Locale.ROOT);
        if (!PNR.matcher(v).matches())
            throw new ServiceException("PNR must be 10 letters/digits.");
        return v;
    }

    public static int parseTrainNumber(String value) throws ServiceException {
        String v = requireNonBlank(value, "Train number");
        if (!v.matches("\\d{1,6}"))
            throw new ServiceException("Train number must be numeric (up to 6 digits).");
        int n = Integer.parseInt(v);
        if (n <= 0) throw new ServiceException("Train number must be greater than zero.");
        return n;
    }
}
