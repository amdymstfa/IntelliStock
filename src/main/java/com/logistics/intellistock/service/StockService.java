package com.logistics.intellistock.service;

import com.logistics.intellistock.dto.request.UpdateStockRequest;
import com.logistics.intellistock.dto.response.StockResponse;

import java.util.List;

public interface StockService {
    List<StockResponse> getStocksByWarehouse(Long warehouseId);
    StockResponse getStockByProductAndWarehouse(Long productId, Long warehouseId);
    StockResponse updateStock(Long productId, Long warehouseId, UpdateStockRequest request);
    List<StockResponse> getLowStockItems();
    List<StockResponse> getLowStockItemsByWarehouse(Long warehouseId);
}
