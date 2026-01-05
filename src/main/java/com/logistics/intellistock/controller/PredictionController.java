package com.logistics.intellistock.controller;

import com.logistics.intellistock.dto.response.PredictionResponse;
import com.logistics.intellistock.service.PredictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/predictions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Predictions", description = "API de prédiction de stock et de recommandations AI")
public class PredictionController {

    private final PredictionService predictionService;

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    @Operation(summary = "Générer une prédiction pour un produit et un entrepôt")
    public ResponseEntity<PredictionResponse> generatePrediction(
            @RequestParam Long productId,
            @RequestParam Long warehouseId) {

        try {
            if (productId == null || productId <= 0) {
                log.warn("ProductId invalide: {}", productId);
                return ResponseEntity.badRequest().build();
            }

            if (warehouseId == null || warehouseId <= 0) {
                log.warn("WarehouseId invalide: {}", warehouseId);
                return ResponseEntity.badRequest().build();
            }

            PredictionResponse response = predictionService.generatePrediction(productId, warehouseId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la génération de prédiction", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/warehouse/{warehouseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    @Operation(summary = "Récupérer toutes les prédictions pour un entrepôt")
    public ResponseEntity<List<PredictionResponse>> getPredictionsByWarehouse(
            @PathVariable Long warehouseId) {

        try {
            if (warehouseId == null || warehouseId <= 0) {
                log.warn("WarehouseId invalide: {}", warehouseId);
                return ResponseEntity.badRequest().build();
            }

            List<PredictionResponse> predictions = predictionService.getPredictionsByWarehouse(warehouseId);
            return ResponseEntity.ok(predictions);

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des prédictions", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/latest")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    @Operation(summary = "Récupérer la dernière prédiction pour un produit et un entrepôt")
    public ResponseEntity<PredictionResponse> getLatestPrediction(
            @RequestParam Long productId,
            @RequestParam Long warehouseId) {

        try {
            if (productId == null || productId <= 0 || warehouseId == null || warehouseId <= 0) {
                log.warn("Paramètres invalides - ProductId: {}, WarehouseId: {}", productId, warehouseId);
                return ResponseEntity.badRequest().build();
            }

            PredictionResponse prediction = predictionService.getLatestPrediction(productId, warehouseId);

            if (prediction == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(prediction);

        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la dernière prédiction", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/generate-all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Générer des prédictions pour tous les produits (admin seulement)")
    public ResponseEntity<Void> generatePredictionsForAllProducts() {
        try {
            log.info("Démarrage de la génération de toutes les prédictions");
            predictionService.generatePredictionsForAllProducts();
            return ResponseEntity.accepted().build();

        } catch (Exception e) {
            log.error("Erreur lors de la génération globale", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/alerts")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    @Operation(summary = "Récupérer les prédictions avec alertes de stock")
    public ResponseEntity<List<PredictionResponse>> getAlertPredictions(
            @RequestParam(required = false) Long warehouseId) {

        try {
            if (warehouseId != null && warehouseId <= 0) {
                log.warn("WarehouseId invalide: {}", warehouseId);
                return ResponseEntity.badRequest().build();
            }

            List<PredictionResponse> alerts = predictionService.getAlertPredictions(warehouseId);
            return ResponseEntity.ok(alerts);

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des alertes", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}