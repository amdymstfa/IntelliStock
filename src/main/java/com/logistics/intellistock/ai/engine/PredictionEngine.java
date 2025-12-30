package com.logistics.intellistock.ai.engine;

import com.logistics.intellistock.ai.analysis.SeasonalityDetector;
import com.logistics.intellistock.ai.analysis.TrendAnalyzer;
import com.logistics.intellistock.ai.model.PredictionContext;
import com.logistics.intellistock.entity.Product;
import com.logistics.intellistock.entity.SalesHistory;
import com.logistics.intellistock.entity.Stock;
import com.logistics.intellistock.entity.Warehouse;
import com.logistics.intellistock.repository.SalesHistoryRepository;
import com.logistics.intellistock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PredictionEngine {

    private final SalesHistoryRepository salesHistoryRepository;
    private final StockRepository stockRepository;
    private final TrendAnalyzer trendAnalyzer;
    private final SeasonalityDetector seasonalityDetector;


    public PredictionContext predictForProductAndWarehouse(Long productId, Long warehouseId,
                                                           int forecastDays) {
        try {
            // Récupérer les données historiques
            LocalDate startDate = LocalDate.now().minusDays(90);
            List<SalesHistory> historicalData = salesHistoryRepository
                    .findByProductIdAndWarehouseIdAndDateRange(productId, warehouseId, startDate);

            if (historicalData.isEmpty()) {
                log.warn("Données insuffisantes pour la prédiction (productId: {}, warehouseId: {})",
                        productId, warehouseId);
                return createDefaultPrediction(productId, warehouseId);
            }

            double trendCoefficient = trendAnalyzer.calculateTrendCoefficient(historicalData);
            String trendDirection = trendAnalyzer.determineTrendDirection(historicalData);

            Map<String, Double> seasonalityFactors = seasonalityDetector.detectSeasonality(historicalData);
            String seasonalityPattern = seasonalityDetector.getSeasonalityPattern(historicalData);
            double seasonalityStrength = seasonalityDetector.calculateSeasonalityStrength(seasonalityFactors);

            double baseForecast = calculateBaseForecast(historicalData, forecastDays);

            double trendAdjusted = applyTrendAdjustment(baseForecast, trendCoefficient);
            LocalDate targetDate = LocalDate.now().plusDays(forecastDays);
            double finalForecast = seasonalityDetector.adjustForSeasonality(
                    trendAdjusted, seasonalityFactors, targetDate);

            BigDecimal confidence = calculateConfidenceLevel(historicalData, trendCoefficient,
                    seasonalityStrength);

            String recommendation = generateRecommendation(finalForecast, productId, warehouseId);

            return PredictionContext.builder()
                    .productId(productId)
                    .warehouseId(warehouseId)
                    .forecastDate(LocalDate.now())
                    .forecastPeriodDays(forecastDays)
                    .predictedQuantity(Math.round(finalForecast))
                    .confidenceLevel(confidence)
                    .recommendation(recommendation)
                    .trendDirection(trendDirection)
                    .trendCoefficient(trendCoefficient)
                    .seasonalityPattern(seasonalityPattern)
                    .historicalDataPoints(historicalData.size())
                    .calculationTimestamp(System.currentTimeMillis())
                    .seasonalityStrength(seasonalityStrength)
                    .build();

        } catch (Exception e) {
            log.error("Erreur lors de la génération de prédiction", e);
            return createErrorPrediction(productId, warehouseId, e.getMessage());
        }
    }


    private double calculateBaseForecast(List<SalesHistory> historicalData, int forecastDays) {
        LocalDate cutoff = LocalDate.now().minusDays(30);
        double recentAverage = historicalData.stream()
                .filter(data -> !data.getSaleDate().isBefore(cutoff))
                .mapToInt(SalesHistory::getQuantitySold)
                .average()
                .orElse(0.0);

        if (recentAverage <= 0) {
            recentAverage = historicalData.stream()
                    .mapToInt(SalesHistory::getQuantitySold)
                    .average()
                    .orElse(0.0);
        }

        double dailyAverage = recentAverage / 30.0; // Moyenne quotidienne
        return dailyAverage * forecastDays;
    }


    private double applyTrendAdjustment(double baseForecast, double trendCoefficient) {
        double boundedCoefficient = Math.max(0.5, Math.min(2.0, trendCoefficient));
        return baseForecast * boundedCoefficient;
    }


    private BigDecimal calculateConfidenceLevel(List<SalesHistory> historicalData,
                                                double trendCoefficient,
                                                double seasonalityStrength) {
        double confidence = 0.0;

        int dataPoints = historicalData.size();
        double dataFactor = Math.min(0.4, dataPoints / 250.0);

        double stabilityFactor = calculateStabilityFactor(historicalData) * 0.3;


        double seasonalityFactor = Math.min(0.2, seasonalityStrength * 0.3);

        double trendFactor = (1.0 - Math.abs(trendCoefficient - 1.0)) * 0.1;

        confidence = dataFactor + stabilityFactor + seasonalityFactor + trendFactor;

        return BigDecimal.valueOf(Math.min(0.99, Math.max(0.1, confidence)))
                .setScale(2, RoundingMode.HALF_UP);
    }


    private double calculateStabilityFactor(List<SalesHistory> historicalData) {
        if (historicalData.size() < 2) return 0.1;

        double mean = historicalData.stream()
                .mapToInt(SalesHistory::getQuantitySold)
                .average()
                .orElse(0.0);

        if (mean == 0) return 0.1;

        double variance = historicalData.stream()
                .mapToInt(SalesHistory::getQuantitySold)
                .mapToDouble(x -> Math.pow(x - mean, 2))
                .average()
                .orElse(0.0);

        double cv = Math.sqrt(variance) / mean;
        return Math.max(0, 1.0 - cv);
    }


    private String generateRecommendation(double predictedQuantity, Long productId, Long warehouseId) {
        // CORRECTION 1: Enlever le cast inutile
        Stock currentStock = stockRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElse(null);

        if (currentStock == null) {
            return "Produit non trouvé dans l'entrepôt";
        }

        int currentQty = currentStock.getQuantityAvailable();
        int alertThreshold = currentStock.getAlertThreshold();
        double needs = predictedQuantity - currentQty;

        if (currentQty <= alertThreshold) {
            int orderQty = Math.max((int) Math.ceil(needs), alertThreshold * 2);
            return String.format("ALERTE: Stock critique (%d unités). Commander immédiatement %d unités.",
                    currentQty, orderQty);
        } else if (needs > 0) {
            int orderQty = (int) Math.ceil(needs * 1.2); // Buffer de 20%
            return String.format("Prévision de %.0f unités. Recommander %d unités pour couvrir la demande.",
                    predictedQuantity, orderQty);
        } else if (currentQty > predictedQuantity * 3) {
            int excess = currentQty - (int) predictedQuantity;
            return String.format("SURSTOCK: %d unités excédentaires. Considérer transfert vers autre entrepôt.",
                    excess);
        } else {
            return "Stock suffisant pour la période prévue.";
        }
    }


    private PredictionContext createDefaultPrediction(Long productId, Long warehouseId) {
        return PredictionContext.builder()
                .productId(productId)
                .warehouseId(warehouseId)
                .forecastDate(LocalDate.now())
                .forecastPeriodDays(30)
                .predictedQuantity(0L)
                .confidenceLevel(BigDecimal.ZERO)
                .recommendation("Données historiques insuffisantes pour générer une prédiction")
                .trendDirection("INSUFFICIENT_DATA")
                .trendCoefficient(1.0)
                .seasonalityPattern("UNKNOWN")
                .historicalDataPoints(0)
                .calculationTimestamp(System.currentTimeMillis())
                .build();
    }


    private PredictionContext createErrorPrediction(Long productId, Long warehouseId, String error) {
        return PredictionContext.builder()
                .productId(productId)
                .warehouseId(warehouseId)
                .forecastDate(LocalDate.now())
                .forecastPeriodDays(30)
                .predictedQuantity(0L)
                .confidenceLevel(BigDecimal.ZERO)
                .recommendation("Erreur de prédiction: " + error)
                .trendDirection("ERROR")
                .trendCoefficient(1.0)
                .seasonalityPattern("ERROR")
                .historicalDataPoints(0)
                .calculationTimestamp(System.currentTimeMillis())
                .build();
    }


    public List<PredictionContext> batchPredict(List<Long> productIds, Long warehouseId) {
        return productIds.stream()
                .map(productId -> predictForProductAndWarehouse(productId, warehouseId, 30))
                .collect(Collectors.toList());
    }
}