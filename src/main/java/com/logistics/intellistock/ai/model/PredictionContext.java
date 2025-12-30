package com.logistics.intellistock.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionContext {

    private Long productId;
    private Long warehouseId;
    private LocalDate forecastDate;
    private Integer forecastPeriodDays;
    private Long predictedQuantity;
    private BigDecimal confidenceLevel;
    private String recommendation;
    private String trendDirection;
    private Double trendCoefficient;
    private String seasonalityPattern;
    private Double seasonalityStrength;
    private Integer historicalDataPoints;
    private Long calculationTimestamp;
    private Long processingTimeMs;

    public boolean isValid() {
        return productId != null &&
                warehouseId != null &&
                forecastDate != null &&
                predictedQuantity != null &&
                confidenceLevel != null &&
                confidenceLevel.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isErrorPrediction() {
        return "ERROR".equals(trendDirection) ||
                confidenceLevel.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isAlertPrediction() {
        return recommendation != null &&
                recommendation.toUpperCase().contains("ALERTE");
    }

    public boolean isHighConfidence() {
        return confidenceLevel.compareTo(new BigDecimal("0.75")) >= 0;
    }

    public String getSummary() {
        return String.format("Prédiction pour produit %d (entrepôt %d): %d unités sur %d jours (confiance: %s%%)",
                productId, warehouseId, predictedQuantity, forecastPeriodDays,
                confidenceLevel.multiply(new BigDecimal("100")).intValue());
    }
}