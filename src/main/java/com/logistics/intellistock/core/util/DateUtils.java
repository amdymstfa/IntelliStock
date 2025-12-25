package com.logistics.intellistock.core.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtils {

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  public static String formatDate(LocalDate date) {
    return date != null ? date.format(DATE_FORMATTER) : null;
  }

  public static String formatDateTime(LocalDateTime dateTime) {
    return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : null;
  }

  public static LocalDate parseDate(String dateString) {
    return dateString != null ? LocalDate.parse(dateString, DATE_FORMATTER) : null;
  }

  public static LocalDateTime parseDateTime(String dateTimeString) {
    return dateTimeString != null ? LocalDateTime.parse(dateTimeString, DATETIME_FORMATTER) : null;
  }

  public static LocalDate getStartOfMonth(LocalDate date) {
    return date.withDayOfMonth(1);
  }

  public static LocalDate getEndOfMonth(LocalDate date) {
    return date.withDayOfMonth(date.lengthOfMonth());
  }

  public static long daysBetween(LocalDate startDate, LocalDate endDate) {
    return ChronoUnit.DAYS.between(startDate, endDate);
  }

  public static LocalDate minusDays(LocalDate date, int days) {
    return date.minusDays(days);
  }

  public static LocalDate plusDays(LocalDate date, int days) {
    return date.plusDays(days);
  }

  public static boolean isWeekend(LocalDate date) {
    java.time.DayOfWeek dayOfWeek = date.getDayOfWeek();
    return dayOfWeek == java.time.DayOfWeek.SATURDAY || dayOfWeek == java.time.DayOfWeek.SUNDAY;
  }

  private DateUtils() {
    throw new IllegalStateException("Utility class");
  }
}
