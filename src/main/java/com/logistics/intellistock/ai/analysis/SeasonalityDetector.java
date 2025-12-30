package com.logistics.intellistock.ai.analysis;

import com.logistics.intellistock.entity.SalesHistory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SeasonalityDetector {

    public Map<String, Double> detectSeasonality(List<SalesHistory> salesHistoryList) {
        if (salesHistoryList == null || salesHistoryList.isEmpty()) {
            log.warn("Aucune donnée de vente disponible pour l'analyse de saisonnalité");
            return Collections.emptyMap();
        }

        Map<String, Double> seasonalityFactors = new HashMap<>();
        seasonalityFactors.putAll(analyzeWeeklyPattern(salesHistoryList));
        seasonalityFactors.putAll(analyzeMonthlyPattern(salesHistoryList));
        seasonalityFactors.putAll(detectPeaks(salesHistoryList));

        log.debug("Analyse de saisonnalité terminée: {} facteurs détectés", seasonalityFactors.size());
        return seasonalityFactors;
    }

    private Map<String, Double> analyzeWeeklyPattern(List<SalesHistory> salesHistoryList) {
        Map<String, Double> weeklyFactors = new HashMap<>();

        Map<String, List<SalesHistory>> salesByDay = salesHistoryList.stream()
                .collect(Collectors.groupingBy(sh -> sh.getDayOfWeek().name()));

        double globalAverage = salesHistoryList.stream()
                .mapToInt(SalesHistory::getQuantitySold)
                .average()
                .orElse(0.0);

        if (globalAverage == 0) {
            return weeklyFactors;
        }

        for (String day : Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY")) {
            List<SalesHistory> daySales = salesByDay.get(day);
            if (daySales != null && !daySales.isEmpty()) {
                double dayAverage = daySales.stream()
                        .mapToInt(SalesHistory::getQuantitySold)
                        .average()
                        .orElse(0.0);

                double factor = dayAverage / globalAverage;
                weeklyFactors.put("DAY_" + day, factor);
            }
        }

        return weeklyFactors;
    }

    private Map<String, Double> analyzeMonthlyPattern(List<SalesHistory> salesHistoryList) {
        Map<String, Double> monthlyFactors = new HashMap<>();

        Map<Integer, List<SalesHistory>> salesByMonth = salesHistoryList.stream()
                .collect(Collectors.groupingBy(SalesHistory::getMonth));

        double globalAverage = salesHistoryList.stream()
                .mapToInt(SalesHistory::getQuantitySold)
                .average()
                .orElse(0.0);

        if (globalAverage == 0) {
            return monthlyFactors;
        }

        for (int month = 1; month <= 12; month++) {
            List<SalesHistory> monthSales = salesByMonth.get(month);
            if (monthSales != null && !monthSales.isEmpty()) {
                double monthAverage = monthSales.stream()
                        .mapToInt(SalesHistory::getQuantitySold)
                        .average()
                        .orElse(0.0);

                double factor = monthAverage / globalAverage;
                monthlyFactors.put("MONTH_" + month, factor);
            }
        }

        return monthlyFactors;
    }

    private Map<String, Double> detectPeaks(List<SalesHistory> salesHistoryList) {
        Map<String, Double> peaks = new HashMap<>();

        Map<LocalDate, Integer> dailySales = new HashMap<>();
        for (SalesHistory sale : salesHistoryList) {
            dailySales.merge(sale.getSaleDate(), sale.getQuantitySold(), Integer::sum);
        }

        if (dailySales.isEmpty()) {
            return peaks;
        }

        List<Integer> salesValues = new ArrayList<>(dailySales.values());
        double mean = salesValues.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        double stdDev = calculateStdDev(salesValues, mean);

        double threshold = mean + (1.5 * stdDev);

        for (Map.Entry<LocalDate, Integer> entry : dailySales.entrySet()) {
            if (entry.getValue() > threshold) {
                double peakFactor = entry.getValue() / mean;
                peaks.put("PEAK_" + entry.getKey(), peakFactor);
            }
        }

        return peaks;
    }

    private double calculateStdDev(List<Integer> values, double mean) {
        if (values.size() <= 1) {
            return 0.0;
        }

        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .sum() / (values.size() - 1);

        return Math.sqrt(variance);
    }

    public String getSeasonalityPattern(List<SalesHistory> salesHistoryList) {
        Map<String, Double> factors = detectSeasonality(salesHistoryList);

        if (factors.isEmpty()) {
            return "NO_PATTERN";
        }

        Optional<Map.Entry<String, Double>> maxEntry = factors.entrySet().stream()
                .max(Map.Entry.comparingByValue());

        if (!maxEntry.isPresent() || maxEntry.get().getValue() <= 1.0) {
            return "STABLE";
        }

        String key = maxEntry.get().getKey();
        double value = maxEntry.get().getValue();

        if (key.startsWith("DAY_")) {
            return "WEEKLY_" + key.substring(4) + "_x" + String.format("%.1f", value);
        } else if (key.startsWith("MONTH_")) {
            return "MONTHLY_" + key.substring(6) + "_x" + String.format("%.1f", value);
        }

        return "COMPLEX_PATTERN";
    }

    public double adjustForSeasonality(double baseForecast, Map<String, Double> seasonalityFactors, LocalDate targetDate) {
        double adjustment = 1.0;

        String dayOfWeek = targetDate.getDayOfWeek().name();
        String dayKey = "DAY_" + dayOfWeek;
        if (seasonalityFactors.containsKey(dayKey)) {
            adjustment *= seasonalityFactors.get(dayKey);
        }

        int month = targetDate.getMonthValue();
        String monthKey = "MONTH_" + month;
        if (seasonalityFactors.containsKey(monthKey)) {
            adjustment *= seasonalityFactors.get(monthKey);
        }

        for (String key : seasonalityFactors.keySet()) {
            if (key.startsWith("PEAK_")) {
                try {
                    LocalDate peakDate = LocalDate.parse(key.substring(5));
                    long daysBetween = Math.abs(java.time.temporal.ChronoUnit.DAYS.between(peakDate, targetDate));
                    if (daysBetween <= 3) {
                        adjustment *= seasonalityFactors.get(key);
                        break;
                    }
                } catch (Exception e) {
                }
            }
        }

        return baseForecast * adjustment;
    }

    public double calculateSeasonalityStrength(Map<String, Double> seasonalityFactors) {
        if (seasonalityFactors.isEmpty()) {
            return 0.0;
        }

        return seasonalityFactors.values().stream()
                .mapToDouble(factor -> Math.abs(factor - 1.0))
                .max()
                .orElse(0.0);
    }
}