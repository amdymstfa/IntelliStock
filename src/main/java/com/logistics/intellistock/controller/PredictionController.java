package com.logistics.intellistock.controller;

import com.logistics.intellistock.dto.response.PredictionResponse;
import com.logistics.intellistock.service.PredictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/predictions")
@RequiredArgsConstructor
@Tag(name = "Predictions", description = "API de prédiction de stock et de recommandations AI")
public class PredictionController {

    private final PredictionService predictionService;

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    @Operation(summary = "Générer une prédiction pour un produit et un entrepôt")
    public ResponseEntity<PredictionResponse> generatePrediction(
            @RequestParam Long productId,
            @RequestParam Long warehouseId) {

        PredictionResponse response = predictionService.generatePrediction(productId, warehouseId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/warehouse/{warehouseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    @Operation(summary = "Récupérer toutes les prédictions pour un entrepôt")
    public ResponseEntity<List<PredictionResponse>> getPredictionsByWarehouse(
            @PathVariable Long warehouseId) {

        List<PredictionResponse> predictions = predictionService.getPredictionsByWarehouse(warehouseId);
        return ResponseEntity.ok(predictions);
    }

    @GetMapping("/latest")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    @Operation(summary = "Récupérer la dernière prédiction pour un produit et un entrepôt")
    public ResponseEntity<PredictionResponse> getLatestPrediction(
            @RequestParam Long productId,
            @RequestParam Long warehouseId) {

        PredictionResponse prediction = predictionService.getLatestPrediction(productId, warehouseId);
        return prediction != null ? ResponseEntity.ok(prediction) : ResponseEntity.notFound().build();
    }

    @PostMapping("/generate-all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Générer des prédictions pour tous les produits (admin seulement)")
    public ResponseEntity<Void> generatePredictionsForAllProducts() {
        predictionService.generatePredictionsForAllProducts();
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/alerts")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    @Operation(summary = "Récupérer les prédictions avec alertes de stock")
    public ResponseEntity<List<PredictionResponse>> getAlertPredictions(
            @RequestParam(required = false) Long warehouseId) {
        return ResponseEntity.ok(Collections.emptyList());

    }
}