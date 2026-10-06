package com.minibank.util;

import java.util.regex.Pattern;

public final class Validator {
    private Validator() {}

    private static final Pattern NAME = Pattern.compile("^[A-Za-z][A-Za-z.'-]*(\\s+[A-Za-z][A-Za-z.'-]*)+$");
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^0[789]\\d{9}$");
    private static final Pattern ELEVEN_DIGITS = Pattern.compile("^\\d{11}$");
    private static final Pattern PIN = Pattern.compile("^\\d{4}$");
    private static final Pattern ACCOUNT_NO = Pattern.compile("^\\d{10}$");

    /**at Least two words(first + lastname), letters only */
    public static boolean isValidFullName(String s) {return s != null && NAME.matcher(s.trim()).matches();}

    /** Email is optional: blank is valid; otherwise it must match the format. */
    public static boolean isValidEmail(String s) {
        return s == null || s.trim().isEmpty() || EMAIL.matcher(s.trim()).matches();
    }

    public static boolean isValidPhone(String s) { return s != null && PHONE.matcher(s).matches(); }
    public static boolean isValidNin(String s) { return s != null && ELEVEN_DIGITS.matcher(s).matches(); }
    public static boolean isValidBvn(String s) { return s != null && ELEVEN_DIGITS.matcher(s).matches(); }
    public static boolean isValidPin(String s) { return s != null && PIN.matcher(s).matches(); }
    public static boolean isValidAccountNumber(String s) { return s != null && ACCOUNT_NO.matcher(s).matches(); }
    public static boolean isValidAddress(String s) { return s != null && s.trim().length() >= 5; }
}
