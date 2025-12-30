package com.logistics.intellistock.ai.llm;

import com.logistics.intellistock.ai.model.PredictionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class AiRecommendationService {

    private static final Map<String, String> RECOMMENDATION_TEMPLATES = new HashMap<>();

    static {
        RECOMMENDATION_TEMPLATES.put("STOCK_CRITICAL",
                " **ALERTE STOCK CRITIQUE** \n" +
                        "Le stock actuel (%d) est en dessous du seuil d'alerte (%d).\n" +
                        "Recommandation: Commander immédiatement **%d unités**.");

        RECOMMENDATION_TEMPLATES.put("NEED_REORDER",
                " **REAPPROVISIONNEMENT NÉCESSAIRE**\n" +
                        "Prévision de demande: **%.0f unités** sur %d jours.\n" +
                        "Stock actuel: %d unités.\n" +
                        "Recommandation: Commander **%d unités** (buffer de sécurité inclus).");

        RECOMMENDATION_TEMPLATES.put("OVERSTOCK",
                " **RISQUE DE SURSTOCK**\n" +
                        "Stock actuel (%d) dépasse 3x la prévision (%.0f).\n" +
                        "Recommandation: Considérer un transfert de **%d unités** vers un autre entrepôt.");

        RECOMMENDATION_TEMPLATES.put("SEASONAL_PEAK",
                "**PIC SAISONNIER DÉTECTÉ**\n" +
                        "Augmentation saisonnière prévue de %.0f%%.\n" +
                        "Recommandation: Augmenter la commande de **%d unités supplémentaires**.");

        RECOMMENDATION_TEMPLATES.put("TREND_UPWARD",
                " **TENDANCE HAUSSIÈRE**\n" +
                        "Croissance détectée: %.1f%% par période.\n" +
                        "Recommandation: Ajuster les commandes futures à la hausse.");

        RECOMMENDATION_TEMPLATES.put("TREND_DOWNWARD",
                " **TENDANCE BAISSIÈRE**\n" +
                        "Déclin détecté: %.1f%% par période.\n" +
                        "Recommandation: Réduire les quantités commandées.");
    }

    public String generateEnhancedRecommendation(PredictionContext context,
                                                 Map<String, Object> additionalData) {
        try {
            String prompt = buildRecommendationPrompt(context, additionalData);



            String recommendation = generateDeterministicRecommendation(context, additionalData);

            if (additionalData != null) {
                recommendation += "\n\n" + generateAdditionalInsights(additionalData);
            }

            recommendation += String.format("\n\n_Prédiction générée le %s avec un niveau de confiance de %s%%_",
                    context.getForecastDate(),
                    context.getConfidenceLevel().multiply(new java.math.BigDecimal("100")).intValue());

            log.debug("Recommandation générée: {}", recommendation.substring(0, Math.min(100, recommendation.length())) + "...");

            return recommendation;

        } catch (Exception e) {
            log.error("Erreur lors de la génération de recommandation", e);
            return "Impossible de générer une recommandation détaillée. " +
                    "Veuillez vous référer aux données de prédiction brutes.";
        }
    }

    private String buildRecommendationPrompt(PredictionContext context,
                                             Map<String, Object> additionalData) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("En tant qu'expert en gestion de stock, analysez la situation suivante et fournissez une recommandation concrète:\n\n");
        prompt.append("CONTEXTE DE PRÉDICTION:\n");
        prompt.append("- Produit ID: ").append(context.getProductId()).append("\n");
        prompt.append("- Entrepôt ID: ").append(context.getWarehouseId()).append("\n");
        prompt.append("- Période de prévision: ").append(context.getForecastPeriodDays()).append(" jours\n");
        prompt.append("- Quantité prévue: ").append(context.getPredictedQuantity()).append(" unités\n");
        prompt.append("- Confiance: ").append(context.getConfidenceLevel().multiply(new java.math.BigDecimal("100"))).append("%\n");
        prompt.append("- Tendance: ").append(context.getTrendDirection()).append(" (coefficient: ").append(context.getTrendCoefficient()).append(")\n");
        prompt.append("- Pattern saisonnier: ").append(context.getSeasonalityPattern()).append("\n");
        prompt.append("- Données historiques: ").append(context.getHistoricalDataPoints()).append(" points\n");

        if (additionalData != null) {
            prompt.append("\nDONNÉES SUPPLÉMENTAIRES:\n");
            additionalData.forEach((key, value) ->
                    prompt.append("- ").append(key).append(": ").append(value).append("\n"));
        }

        prompt.append("\nGÉNÈRE UNE RECOMMANDATION DÉTAILLÉE INCLUANT:\n");
        prompt.append("1. Quantité à commander (si applicable)\n");
        prompt.append("2. Délai recommandé\n");
        prompt.append("3. Considérations de risque\n");
        prompt.append("4. Actions alternatives\n");
        prompt.append("5. Priorité (Haute/Moyenne/Basse)\n");

        return prompt.toString();
    }


    private String generateDeterministicRecommendation(PredictionContext context,
                                                       Map<String, Object> additionalData) {
        if (context.getRecommendation() != null) {
            if (context.getRecommendation().contains("ALERTE")) {
                return String.format(RECOMMENDATION_TEMPLATES.get("STOCK_CRITICAL"),
                        getStockQuantity(additionalData),
                        getAlertThreshold(additionalData),
                        calculateOrderQuantity(context, additionalData));
            } else if (context.getRecommendation().contains("Commander")) {
                return String.format(RECOMMENDATION_TEMPLATES.get("NEED_REORDER"),
                        context.getPredictedQuantity().doubleValue(),
                        context.getForecastPeriodDays(),
                        getStockQuantity(additionalData),
                        calculateOrderQuantity(context, additionalData));
            } else if (context.getRecommendation().contains("SURSTOCK")) {
                double predicted = context.getPredictedQuantity().doubleValue();
                int current = getStockQuantity(additionalData);
                int excess = current - (int)(predicted * 1.5);
                return String.format(RECOMMENDATION_TEMPLATES.get("OVERSTOCK"),
                        current, predicted, Math.max(0, excess));
            }
        }

        if (context.getTrendCoefficient() > 1.1) {
            double growth = (context.getTrendCoefficient() - 1) * 100;
            return String.format(RECOMMENDATION_TEMPLATES.get("TREND_UPWARD"), growth);
        } else if (context.getTrendCoefficient() < 0.9) {
            double decline = (1 - context.getTrendCoefficient()) * 100;
            return String.format(RECOMMENDATION_TEMPLATES.get("TREND_DOWNWARD"), decline);
        }

        return "**ANALYSE DE STOCK STANDARD**\n" +
                "La situation actuelle ne nécessite pas d'action immédiate.\n" +
                "Recommandation: Surveiller les ventes quotidiennes et réévaluer dans 7 jours.";
    }


    private String generateAdditionalInsights(Map<String, Object> additionalData) {
        StringBuilder insights = new StringBuilder();
        insights.append(" **INSIGHTS SUPPLÉMENTAIRES:**\n");

        if (additionalData.containsKey("supplier_lead_time")) {
            int leadTime = (int) additionalData.get("supplier_lead_time");
            insights.append("- Délai fournisseur: ").append(leadTime).append(" jours\n");

            if (leadTime > 14) {
                insights.append("  → Pensez à commander plus tôt pour éviter les ruptures\n");
            }
        }

        if (additionalData.containsKey("storage_cost_per_unit")) {
            double cost = (double) additionalData.get("storage_cost_per_unit");
            insights.append("- Coût de stockage: ").append(String.format("%.2f", cost)).append(" €/unité/mois\n");
        }

        if (additionalData.containsKey("last_order_date")) {
            insights.append("- Dernière commande: ").append(additionalData.get("last_order_date")).append("\n");
        }

        if (additionalData.containsKey("similar_products_demand")) {
            insights.append("- Demande produits similaires: ").append(additionalData.get("similar_products_demand")).append("\n");
        }

        return insights.toString();
    }

    private int getStockQuantity(Map<String, Object> additionalData) {
        if (additionalData != null && additionalData.containsKey("current_stock")) {
            return (int) additionalData.get("current_stock");
        }
        return 0;
    }

    private int getAlertThreshold(Map<String, Object> additionalData) {
        if (additionalData != null && additionalData.containsKey("alert_threshold")) {
            return (int) additionalData.get("alert_threshold");
        }
        return 10;
    }

    private int calculateOrderQuantity(PredictionContext context, Map<String, Object> additionalData) {
        double predicted = context.getPredictedQuantity().doubleValue();
        int current = getStockQuantity(additionalData);
        int safetyBuffer = (int) (predicted * 0.2);
        return Math.max(0, (int) Math.ceil(predicted - current + safetyBuffer));
    }
}