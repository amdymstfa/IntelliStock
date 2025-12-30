package com.logistics.intellistock.ai;

import java.util.Map;

public class AIConstants {

    public static final int MIN_HISTORICAL_DATA_POINTS = 7;
    public static final int DEFAULT_FORECAST_DAYS = 30;
    public static final double HIGH_CONFIDENCE_THRESHOLD = 0.75;
    public static final double MEDIUM_CONFIDENCE_THRESHOLD = 0.5;
    public static final double CRITICAL_STOCK_THRESHOLD = 0.3;

    public static final Map<String, String> SEASONALITY_PATTERNS = Map.of(
            "WEEKLY_MONDAY_x1.5", "Pic du lundi",
            "WEEKLY_FRIDAY_x1.8", "Pic du vendredi",
            "MONTHLY_12_x2.0", "Pic de décembre (Noël)",
            "MONTHLY_1_x1.5", "Pic de janvier (Soldes)"
    );
}