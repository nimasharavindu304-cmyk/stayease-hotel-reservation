package com.stayease.util;

import java.util.regex.Pattern;

/** Small collection of reusable form-validation helpers used across the Swing UIs. */
public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{9}$");

    private ValidationUtil() {
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        return isBlank(email) || EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isPositiveNumber(String text) {
        try {
            return Double.parseDouble(text) > 0;
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
    }
}
