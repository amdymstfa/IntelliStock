package com.logistics.intellistock.service.impl;

import com.logistics.intellistock.ai.engine.PredictionEngine;
import com.logistics.intellistock.ai.llm.AiRecommendationService;
import com.logistics.intellistock.ai.model.PredictionContext;
import com.logistics.intellistock.dto.response.PredictionResponse;
import com.logistics.intellistock.entity.Prediction;
import com.logistics.intellistock.entity.Product;
import com.logistics.intellistock.entity.Stock;
import com.logistics.intellistock.entity.Warehouse;
import com.logistics.intellistock.mapper.PredictionMapper;
import com.logistics.intellistock.repository.PredictionRepository;
import com.logistics.intellistock.repository.ProductRepository;
import com.logistics.intellistock.repository.StockRepository;
import com.logistics.intellistock.repository.WarehouseRepository;
import com.logistics.intellistock.service.PredictionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PredictionServiceImpl implements PredictionService {

    private final PredictionEngine predictionEngine;
    private final AiRecommendationService aiRecommendationService;
    private final PredictionRepository predictionRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockRepository stockRepository;
    private final PredictionMapper predictionMapper;

    @Override
    @Transactional
    public PredictionResponse generatePrediction(Long productId, Long warehouseId) {
        log.info("Génération de prédiction pour produit: {}, entrepôt: {}", productId, warehouseId);


        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé avec id: " + productId));

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("Entrepôt non trouvé avec id: " + warehouseId));


        PredictionContext context = predictionEngine.predictForProductAndWarehouse(productId, warehouseId, 30);


        Map<String, Object> additionalData = gatherAdditionalData(product, warehouse);
        String enhancedRecommendation = aiRecommendationService.generateEnhancedRecommendation(context, additionalData);


        Prediction prediction = savePredictionToDatabase(context, enhancedRecommendation, product, warehouse);

        return predictionMapper.toResponse(prediction);
    }

    @Override
    public List<PredictionResponse> getPredictionsByWarehouse(Long warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("Entrepôt non trouvé avec id: " + warehouseId));

        List<Prediction> predictions = predictionRepository.findByWarehouse(warehouse);

        return predictions.stream()
                .map(predictionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PredictionResponse getLatestPrediction(Long productId, Long warehouseId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé avec id: " + productId));

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("Entrepôt non trouvé avec id: " + warehouseId));

        Optional<Prediction> latestPrediction = predictionRepository
                .findTopByProductAndWarehouseOrderByPredictionDateDesc(product, warehouse);

        return latestPrediction.map(predictionMapper::toResponse).orElse(null);
    }

    @Override
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void generatePredictionsForAllProducts() {
        log.info("Début de la génération des prédictions pour tous les produits");

        List<Product> products = productRepository.findByIsActive(true);
        List<Warehouse> warehouses = warehouseRepository.findByIsActive(true);

        int totalPredictions = products.size() * warehouses.size();
        int completed = 0;

        for (Product product : products) {
            for (Warehouse warehouse : warehouses) {
                try {
                    generatePrediction(product.getId(), warehouse.getId());
                    completed++;

                    if (completed % 10 == 0) {
                        log.info("Progression: {}/{} prédictions générées", completed, totalPredictions);
                    }
                } catch (Exception e) {
                    log.error("Erreur lors de la génération pour produit {} et entrepôt {}: {}",
                            product.getId(), warehouse.getId(), e.getMessage());
                }
            }
        }

        log.info("Génération terminée: {}/{} prédictions générées avec succès", completed, totalPredictions);
    }

    private Map<String, Object> gatherAdditionalData(Product product, Warehouse warehouse) {
        Map<String, Object> additionalData = new java.util.HashMap<>();

        try {
            additionalData.put("product_category", product.getCategory());
            additionalData.put("product_name", product.getName());
            additionalData.put("product_sku", product.getSku());

            additionalData.put("warehouse_city", warehouse.getCity());
            additionalData.put("warehouse_capacity", warehouse.getCapacity());
            additionalData.put("warehouse_name", warehouse.getName());

            Optional<Stock> stockOpt = stockRepository.findByProductAndWarehouse(product, warehouse);
            stockOpt.ifPresent(stock -> {
                additionalData.put("current_stock", stock.getQuantityAvailable());
                additionalData.put("alert_threshold", stock.getAlertThreshold());
                if (stock.getLastRestockedAt() != null) {
                    additionalData.put("last_restocked", stock.getLastRestockedAt());
                }
            });

            additionalData.put("seasonal_factor", calculateSeasonalFactor());
            additionalData.put("generation_date", LocalDateTime.now());

        } catch (Exception e) {
            log.warn("Impossible de collecter des données supplémentaires: {}", e.getMessage());
        }

        return additionalData;
    }

    private Prediction savePredictionToDatabase(PredictionContext context, String enhancedRecommendation,
                                                Product product, Warehouse warehouse) {
        Prediction prediction = Prediction.builder()
                .product(product)
                .warehouse(warehouse)
                .predictionDate(context.getForecastDate() != null ? context.getForecastDate() : LocalDate.now())
                .predictedQuantity30Days(context.getPredictedQuantity() != null ? context.getPredictedQuantity().intValue() : 0)
                .confidenceLevel(context.getConfidenceLevel() != null ? context.getConfidenceLevel() : BigDecimal.ZERO)
                .recommendation(enhancedRecommendation != null ? enhancedRecommendation : "Pas de recommandation")
                .createdAt(LocalDateTime.now())
                .build();

        return predictionRepository.save(prediction);
    }

    private Double calculateSeasonalFactor() {
        int currentMonth = LocalDate.now().getMonthValue();

        Map<Integer, Double> seasonalFactors = new HashMap<>();
        seasonalFactors.put(12, 1.5);
        seasonalFactors.put(1, 1.2);
        seasonalFactors.put(6, 0.8);
        seasonalFactors.put(7, 0.7);

        return seasonalFactors.getOrDefault(currentMonth, 1.0);
    }

    @Override
    public List<PredictionResponse> getAlertPredictions(Long warehouseId) {
        try {
            List<PredictionResponse> allPredictions;

            if (warehouseId != null) {
                allPredictions = getPredictionsByWarehouse(warehouseId);
            } else {
                allPredictions = predictionRepository.findAll()
                        .stream()
                        .map(predictionMapper::toResponse)
                        .collect(Collectors.toList());
            }

            return allPredictions.stream()
                    .filter(p -> p.getRecommendation() != null &&
                            (p.getRecommendation().contains("ALERTE") ||
                                    p.getRecommendation().contains("CRITIQUE") ||
                                    p.getRecommendation().contains("SURSTOCK")))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des alertes", e);
            return Collections.emptyList();
        }
    }
}