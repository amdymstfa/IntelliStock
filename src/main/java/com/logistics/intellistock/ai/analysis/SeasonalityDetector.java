package com.logistics.intellistock.ai.analysis;

import com.logistics.intellistock.entity.SalesHistory;
import com.logistics.intellistock.entity.enums.DayOfWeek;
import com.logistics.intellistock.repository.SalesHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeasonalityDetector {

    private final SalesHistoryRepository salesHistoryRepository;


    public Map<DayOfWeek, Double> detectWeeklySeasonality(Long productId, Long warehouseId, int analysisMonths) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(analysisMonths);

        List<SalesHistory> sales = salesHistoryRepository
                .findByProductIdAndWarehouseIdAndSaleDateBetween(
                        productId, warehouseId, startDate, endDate);

        if (sales.isEmpty()) {
            log.warn("No sales data found for product {} in warehouse {} in the last {} months",
                    productId, warehouseId, analysisMonths);
            return Collections.emptyMap();
        }

        Map<DayOfWeek, List<Integer>> salesByDay = sales.stream()
                .collect(Collectors.groupingBy(
                        SalesHistory::getDayOfWeek,
                        Collectors.mapping(SalesHistory::getQuantitySold, Collectors.toList())
                ));

        Map<DayOfWeek, Double> weeklyPattern = new HashMap<>();

        for (DayOfWeek day : DayOfWeek.values()) {
            List<Integer> daySales = salesByDay.getOrDefault(day, Collections.emptyList());
            double average = daySales.stream().mapToInt(Integer::intValue).average().orElse(0.0);
            weeklyPattern.put(day, average);
        }

        log.info("Weekly seasonality pattern detected for product {}: {}", productId, weeklyPattern);
        return weeklyPattern;
    }


    public Map<Integer, Double> detectMonthlySeasonality(Long productId, Long warehouseId, int analysisYears) {
        int currentYear = LocalDate.now().getYear();
        int startYear = currentYear - analysisYears;

        List<SalesHistory> sales = salesHistoryRepository
                .findByProductIdAndWarehouseIdAndYearBetween(
                        productId, warehouseId, startYear, currentYear);

        if (sales.isEmpty()) {
            log.warn("No sales data found for product {} in warehouse {} in the last {} years",
                    productId, warehouseId, analysisYears);
            return Collections.emptyMap();
        }

        Map<Integer, List<Integer>> salesByMonth = sales.stream()
                .collect(Collectors.groupingBy(
                        SalesHistory::getMonth,
                        Collectors.mapping(SalesHistory::getQuantitySold, Collectors.toList())
                ));

        Map<Integer, Double> monthlyPattern = new HashMap<>();

        for (int month = 1; month <= 12; month++) {
            List<Integer> monthSales = salesByMonth.getOrDefault(month, Collections.emptyList());
            double average = monthSales.stream().mapToInt(Integer::intValue).average().orElse(0.0);
            monthlyPattern.put(month, average);
        }

        log.info("Monthly seasonality pattern detected for product {}: {}", productId, monthlyPattern);
        return monthlyPattern;
    }


    public Map<String, Double> calculateSeasonalCoefficients(Long productId, Long warehouseId) {
        Map<DayOfWeek, Double> weeklyPattern = detectWeeklySeasonality(productId, warehouseId, 6);
        Map<Integer, Double> monthlyPattern = detectMonthlySeasonality(productId, warehouseId, 3);

        if (weeklyPattern.isEmpty() && monthlyPattern.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Double> coefficients = new HashMap<>();

        double weeklyAvg = weeklyPattern.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(1.0);

        for (Map.Entry<DayOfWeek, Double> entry : weeklyPattern.entrySet()) {
            double coefficient = weeklyAvg > 0 ? entry.getValue() / weeklyAvg : 1.0;
            coefficients.put("WEEKLY_" + entry.getKey(), coefficient);
        }

        double monthlyAvg = monthlyPattern.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(1.0);

        for (Map.Entry<Integer, Double> entry : monthlyPattern.entrySet()) {
            double coefficient = monthlyAvg > 0 ? entry.getValue() / monthlyAvg : 1.0;
            coefficients.put("MONTHLY_" + entry.getKey(), coefficient);
        }

        detectSeasonalPeaks(weeklyPattern, monthlyPattern, coefficients);

        log.info("Seasonal coefficients calculated for product {}: {}", productId, coefficients);
        return coefficients;
    }


    private void detectSeasonalPeaks(
            Map<DayOfWeek, Double> weeklyPattern,
            Map<Integer, Double> monthlyPattern,
            Map<String, Double> coefficients) {

        Optional<Map.Entry<DayOfWeek, Double>> maxWeekly = weeklyPattern.entrySet().stream()
                .max(Map.Entry.comparingByValue());

        Optional<Map.Entry<Integer, Double>> maxMonthly = monthlyPattern.entrySet().stream()
                .max(Map.Entry.comparingByValue());

        maxWeekly.ifPresent(entry -> {
            double avg = weeklyPattern.values().stream().mapToDouble(Double::doubleValue).average().orElse(0);
            if (entry.getValue() > avg * 1.5) { // 50% au-dessus de la moyenne
                coefficients.put("PEAK_DAY", (double) entry.getKey().ordinal());
            }
        });

        maxMonthly.ifPresent(entry -> {
            double avg = monthlyPattern.values().stream().mapToDouble(Double::doubleValue).average().orElse(0);
            if (entry.getValue() > avg * 1.5) { // 50% au-dessus de la moyenne
                coefficients.put("PEAK_MONTH", entry.getKey().doubleValue());
            }
        });
    }


    public String getSeasonalityInsights(Long productId, Long warehouseId) {
        Map<String, Double> coefficients = calculateSeasonalCoefficients(productId, warehouseId);

        if (coefficients.isEmpty()) {
            return "Données insuffisantes pour détecter la saisonnalité";
        }

        StringBuilder insights = new StringBuilder();
        insights.append("Analyse de saisonnalité :\n");

        coefficients.entrySet().stream()
                .filter(e -> e.getKey().startsWith("WEEKLY_"))
                .filter(e -> e.getValue() > 1.2)
                .forEach(e -> {
                    String day = e.getKey().replace("WEEKLY_", "");
                    insights.append(String.format("- Fortes ventes le %s (coefficient: %.2f)\n",
                            day, e.getValue()));
                });

        coefficients.entrySet().stream()
                .filter(e -> e.getKey().startsWith("MONTHLY_"))
                .filter(e -> e.getValue() > 1.3)
                .forEach(e -> {
                    int month = Integer.parseInt(e.getKey().replace("MONTHLY_", ""));
                    insights.append(String.format("- Pics saisonniers en mois %d (coefficient: %.2f)\n",
                            month, e.getValue()));
                });

        if (coefficients.containsKey("PEAK_DAY")) {
            int peakDay = coefficients.get("PEAK_DAY").intValue();
            insights.append(String.format("Pics de ventes détectés le %s - anticiper les stocks\n",
                    DayOfWeek.values()[peakDay]));
        }

        if (coefficients.containsKey("PEAK_MONTH")) {
            int peakMonth = coefficients.get("PEAK_MONTH").intValue();
            insights.append(String.format("Saisonnalité mensuelle forte en %s - augmenter les stocks\n",
                    getMonthName(peakMonth)));
        }

        return insights.toString();
    }

    private String getMonthName(int month) {
        String[] monthNames = {"Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};
        return month >= 1 && month <= 12 ? monthNames[month - 1] : "Mois " + month;
    }
}