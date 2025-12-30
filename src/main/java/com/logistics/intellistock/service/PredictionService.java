package com.logistics.intellistock.service;

import com.logistics.intellistock.dto.response.PredictionResponse;

import java.util.List;

public interface PredictionService {
    PredictionResponse generatePrediction(Long productId, Long warehouseId);
    List<PredictionResponse> getPredictionsByWarehouse(Long warehouseId);
    PredictionResponse getLatestPrediction(Long productId, Long warehouseId);
    void generatePredictionsForAllProducts();
}