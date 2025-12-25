package com.logistics.intellistock.core.util;

import java.util.regex.Pattern;

public class ValidationUtils {

  private static final Pattern EMAIL_PATTERN = Pattern.compile(
    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
  );

  private static final Pattern PASSWORD_PATTERN = Pattern.compile(
    "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$"
  );

  private static final Pattern PHONE_PATTERN = Pattern.compile(
    "^\\+?[1-9]\\d{1,14}$"
  );

  public static boolean isValidEmail(String email) {
    return email != null && EMAIL_PATTERN.matcher(email).matches();
  }

  public static boolean isValidPassword(String password) {
    return password != null && PASSWORD_PATTERN.matcher(password).matches();
  }

  public static boolean isValidPhone(String phone) {
    return phone != null && PHONE_PATTERN.matcher(phone).matches();
  }

  public static boolean isNullOrEmpty(String str) {
    return str == null || str.trim().isEmpty();
  }

  public static boolean isPositive(Integer number) {
    return number != null && number > 0;
  }

  public static boolean isPositiveOrZero(Integer number) {
    return number != null && number >= 0;
  }

  private ValidationUtils() {
    throw new IllegalStateException("Utility class");
  }
}

