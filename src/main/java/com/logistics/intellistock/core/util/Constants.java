package com.logistics.intellistock.core.util;

public class Constants {

  // JWT Constants
  public static final String TOKEN_PREFIX = "Bearer ";
  public static final String HEADER_STRING = "Authorization";
  public static final String AUTHORITIES_KEY = "roles";

  // API Paths
  public static final String API_BASE_PATH = "/api/v1";
  public static final String AUTH_PATH = API_BASE_PATH + "/auth";
  public static final String USER_PATH = API_BASE_PATH + "/users";
  public static final String PRODUCT_PATH = API_BASE_PATH + "/products";
  public static final String WAREHOUSE_PATH = API_BASE_PATH + "/warehouses";
  public static final String STOCK_PATH = API_BASE_PATH + "/stocks";
  public static final String SALES_PATH = API_BASE_PATH + "/sales";
  public static final String PREDICTION_PATH = API_BASE_PATH + "/predictions";

  // Validation Messages
  public static final String INVALID_EMAIL = "Invalid email format";
  public static final String REQUIRED_FIELD = "This field is required";
  public static final String PASSWORD_PATTERN = "Password must contain at least 8 characters, one uppercase, one lowercase, one number and one special character";

  // Pagination
  public static final int DEFAULT_PAGE_SIZE = 20;
  public static final int MAX_PAGE_SIZE = 100;

  // AI Prediction
  public static final int DEFAULT_PREDICTION_DAYS = 30;
  public static final double MIN_CONFIDENCE_LEVEL = 0.0;
  public static final double MAX_CONFIDENCE_LEVEL = 1.0;

  private Constants() {
    throw new IllegalStateException("Utility class");
  }
}
