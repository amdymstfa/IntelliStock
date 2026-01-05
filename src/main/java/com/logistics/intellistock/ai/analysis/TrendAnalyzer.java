package com.logistics.intellistock.ai.analysis;

import com.logistics.intellistock.entity.SalesHistory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TrendAnalyzer {

    public double calculateTrendCoefficient(List<SalesHistory> salesHistory) {
        if (salesHistory == null || salesHistory.size() < 7) {
            log.debug("Données insuffisantes pour l'analyse de tendance");
            return 1.0;
        }

        List<SalesHistory> sortedData = salesHistory.stream()
                .sorted(Comparator.comparing(SalesHistory::getSaleDate))
                .collect(Collectors.toList());

        int periodSize = sortedData.size() / 3;
        if (periodSize < 2) {
            return 1.0;
        }

        List<SalesHistory> firstPeriod = sortedData.subList(0, periodSize);
        List<SalesHistory> secondPeriod = sortedData.subList(periodSize, periodSize * 2);
        List<SalesHistory> thirdPeriod = sortedData.subList(periodSize * 2,
                Math.min(periodSize * 3, sortedData.size()));

        double avg1 = calculatePeriodAverage(firstPeriod);
        double avg2 = calculatePeriodAverage(secondPeriod);
        double avg3 = calculatePeriodAverage(thirdPeriod);

        if (avg1 == 0 || avg2 == 0) {
            return 1.0;
        }

        double slope1to2 = (avg2 - avg1) / avg1;
        double slope2to3 = (avg3 - avg2) / avg2;

        double weightedSlope = (slope1to2 * 0.4) + (slope2to3 * 0.6);
        double trendCoefficient = 1.0 + weightedSlope;

        trendCoefficient = Math.max(0.5, Math.min(2.0, trendCoefficient));

        log.debug("Coefficient de tendance calculé: {} (pentes: {}, {})",
                trendCoefficient, slope1to2, slope2to3);

        return trendCoefficient;
    }

    public String determineTrendDirection(List<SalesHistory> salesHistory) {
        double coefficient = calculateTrendCoefficient(salesHistory);

        if (coefficient > 1.1) {
            return "STRONG_UPWARD";
        } else if (coefficient > 1.02) {
            return "UPWARD";
        } else if (coefficient < 0.9) {
            return "STRONG_DOWNWARD";
        } else if (coefficient < 0.98) {
            return "DOWNWARD";
        } else {
            return "STABLE";
        }
    }

    public double calculateLinearRegressionSlope(List<SalesHistory> salesHistory) {
        if (salesHistory.size() < 2) {
            return 0.0;
        }

        List<SalesHistory> sortedData = salesHistory.stream()
                .sorted(Comparator.comparing(SalesHistory::getSaleDate))
                .collect(Collectors.toList());

        LocalDate firstDate = sortedData.get(0).getSaleDate();
        List<Double> xValues = new ArrayList<>();
        List<Double> yValues = new ArrayList<>();

        for (SalesHistory sale : sortedData) {
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(firstDate, sale.getSaleDate());
            xValues.add((double) daysBetween);
            yValues.add((double) sale.getQuantitySold());
        }

        return calculateSlope(xValues, yValues);
    }

    public List<LocalDate> detectTrendBreaks(List<SalesHistory> salesHistory, double threshold) {
        List<LocalDate> breakPoints = new ArrayList<>();

        if (salesHistory.size() < 10) {
            return breakPoints;
        }

        List<SalesHistory> sortedData = salesHistory.stream()
                .sorted(Comparator.comparing(SalesHistory::getSaleDate))
                .collect(Collectors.toList());

        int windowSize = Math.min(7, sortedData.size() / 3);

        for (int i = windowSize; i < sortedData.size() - windowSize; i++) {
            List<SalesHistory> beforeWindow = sortedData.subList(i - windowSize, i);
            List<SalesHistory> afterWindow = sortedData.subList(i, i + windowSize);

            double avgBefore = calculatePeriodAverage(beforeWindow);
            double avgAfter = calculatePeriodAverage(afterWindow);

            if (avgBefore > 0) {
                double change = Math.abs(avgAfter - avgBefore) / avgBefore;

                if (change > threshold) {
                    breakPoints.add(sortedData.get(i).getSaleDate());
                    log.info("Rupture de tendance détectée à la date {}: changement de {}%",
                            sortedData.get(i).getSaleDate(), change * 100);
                }
            }
        }

        return breakPoints;
    }

    private double calculatePeriodAverage(List<SalesHistory> periodData) {
        if (periodData.isEmpty()) {
            return 0.0;
        }

        return periodData.stream()
                .mapToInt(SalesHistory::getQuantitySold)
                .average()
                .orElse(0.0);
    }

    private double calculateSlope(List<Double> xValues, List<Double> yValues) {
        int n = xValues.size();

        double sumX = 0.0;
        double sumY = 0.0;
        double sumXY = 0.0;
        double sumX2 = 0.0;

        for (int i = 0; i < n; i++) {
            sumX += xValues.get(i);
            sumY += yValues.get(i);
            sumXY += xValues.get(i) * yValues.get(i);
            sumX2 += xValues.get(i) * xValues.get(i);
        }

        double numerator = (n * sumXY) - (sumX * sumY);
        double denominator = (n * sumX2) - (sumX * sumX);

        if (denominator == 0) {
            return 0.0;
        }

        return numerator / denominator;
    }

    public double calculateTrendStrength(List<SalesHistory> salesHistory) {
        if (salesHistory.size() < 3) {
            return 0.0;
        }

        List<SalesHistory> sortedData = salesHistory.stream()
                .sorted(Comparator.comparing(SalesHistory::getSaleDate))
                .collect(Collectors.toList());

        LocalDate firstDate = sortedData.get(0).getSaleDate();
        List<Double> xValues = new ArrayList<>();
        List<Double> yValues = new ArrayList<>();

        for (SalesHistory sale : sortedData) {
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(firstDate, sale.getSaleDate());
            xValues.add((double) daysBetween);
            yValues.add((double) sale.getQuantitySold());
        }

        return calculateRSquared(xValues, yValues);
    }

    private double calculateRSquared(List<Double> xValues, List<Double> yValues) {
        double slope = calculateSlope(xValues, yValues);
        double intercept = calculateIntercept(xValues, yValues, slope);
        double yMean = yValues.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        double ssTot = 0.0;
        double ssRes = 0.0;

        for (int i = 0; i < xValues.size(); i++) {
            double yPred = slope * xValues.get(i) + intercept;
            ssTot += Math.pow(yValues.get(i) - yMean, 2);
            ssRes += Math.pow(yValues.get(i) - yPred, 2);
        }

        if (ssTot == 0) {
            return 0.0;
        }

        return 1.0 - (ssRes / ssTot);
    }
    private double calculateIntercept(List<Double> xValues, List<Double> yValues, double slope) {
        double xMean = xValues.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double yMean = yValues.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        return yMean - (slope * xMean);
    }
}
