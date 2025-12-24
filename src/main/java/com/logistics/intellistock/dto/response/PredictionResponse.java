package com.logistics.intellistock.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private Long warehouseId;
    private String warehouseName;
    private LocalDate predictionDate;
    private Integer predictedQuantity30Days;
    private BigDecimal confidenceLevel;
    private String recommendation;
    private Integer currentStock;
    private Integer alertThreshold;
}
