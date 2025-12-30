package com.logistics.intellistock.ai;

import com.logistics.intellistock.ai.model.PredictionContext;
import com.logistics.intellistock.entity.Stock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class RecommendationGenerator {

    public String generateStockRecommendation(PredictionContext context, Stock currentStock) {
        if (currentStock == null) {
            return " Produit non trouvé dans l'entrepôt";
        }

        long predictedQty = context.getPredictedQuantity();
        int currentQty = currentStock.getQuantityAvailable();
        int alertThreshold = currentStock.getAlertThreshold();

        Map<String, Object> analysis = analyzeStockSituation(predictedQty, currentQty, alertThreshold);

        return buildRecommendationMessage(analysis, context, currentStock);
    }

    private Map<String, Object> analyzeStockSituation(long predictedQty, int currentQty, int alertThreshold) {
        double coverageRatio = currentQty / (double) Math.max(predictedQty, 1);
        boolean isCritical = currentQty <= alertThreshold;
        boolean needsReorder = predictedQty > currentQty;
        boolean isOverstock = currentQty > predictedQty * 3;

        int recommendedOrderQty = calculateRecommendedOrder(predictedQty, currentQty, isCritical);

        Map<String, Object> analysis = new HashMap<>();
        analysis.put("coverageRatio", coverageRatio);
        analysis.put("isCritical", isCritical);
        analysis.put("needsReorder", needsReorder);
        analysis.put("isOverstock", isOverstock);
        analysis.put("recommendedOrderQty", recommendedOrderQty);
        analysis.put("daysOfCoverage", (int) (currentQty / (predictedQty / 30.0)));

        return analysis;
    }

    private String buildRecommendationMessage(Map<String, Object> analysis,
                                              PredictionContext context,
                                              Stock currentStock) {

        StringBuilder recommendation = new StringBuilder();

        if ((boolean) analysis.get("isCritical")) {
            recommendation.append(" **ALERTE STOCK CRITIQUE**\n");
            recommendation.append(String.format(
                    "Stock actuel: %d (seuil: %d)\n",
                    currentStock.getQuantityAvailable(),
                    currentStock.getAlertThreshold()
            ));
            recommendation.append(String.format(
                    "Commander immédiatement **%d unités**\n",
                    analysis.get("recommendedOrderQty")
            ));
        } else if ((boolean) analysis.get("needsReorder")) {
            recommendation.append(" **REAPPROVISIONNEMENT REQUIS**\n");
            recommendation.append(String.format(
                    "Prévision: %d unités | Stock: %d unités\n",
                    context.getPredictedQuantity(),
                    currentStock.getQuantityAvailable()
            ));
            recommendation.append(String.format(
                    "Commander **%d unités**\n",
                    analysis.get("recommendedOrderQty")
            ));
        } else if ((boolean) analysis.get("isOverstock")) {
            recommendation.append("**RISQUE DE SURSTOCK**\n");
            int excess = currentStock.getQuantityAvailable()
                    - (int) (context.getPredictedQuantity() * 1.5);
            recommendation.append(String.format(
                    "Excess estimé: %d unités\n",
                    excess
            ));
            recommendation.append("Considérer un transfert vers un autre entrepôt\n");
        } else {
            recommendation.append("**STOCK ADÉQUAT**\n");
            recommendation.append(String.format(
                    "Couverture: %d jours | Ratio: %.1f\n",
                    analysis.get("daysOfCoverage"),
                    analysis.get("coverageRatio")
            ));
            recommendation.append("Aucune action immédiate requise\n");
        }

        if (context.getTrendCoefficient() > 1.05) {
            recommendation.append(String.format(
                    "\n Tendance haussière détectée (+%.1f%%) - Surveiller de près",
                    (context.getTrendCoefficient() - 1) * 100
            ));
        } else if (context.getTrendCoefficient() < 0.95) {
            recommendation.append(String.format(
                    "\nTendance baissière détectée (%.1f%%) - Réduire les commandes futures",
                    (1 - context.getTrendCoefficient()) * 100
            ));
        }

        return recommendation.toString();
    }

    private int calculateRecommendedOrder(long predictedQty, int currentQty, boolean isCritical) {
        double safetyBuffer = isCritical ? 0.3 : 0.2;
        double needed = Math.max(0, predictedQty - currentQty);
        return (int) Math.ceil(needed * (1 + safetyBuffer));
    }
}
