package com.logistics.intellistock.service;

import com.logistics.intellistock.dto.request.CreateSaleRequest;
import com.logistics.intellistock.dto.response.SalesHistoryResponse;

import java.time.LocalDate;
import java.util.List;

public interface SalesHistoryService {
    SalesHistoryResponse recordSale(CreateSaleRequest request);
    List<SalesHistoryResponse> getSalesByWarehouse(Long warehouseId);
    List<SalesHistoryResponse> getSalesByProduct(Long productId);
    List<SalesHistoryResponse> getSalesByWarehouseBetweenDates(
            Long warehouseId, 
            LocalDate startDate, 
            LocalDate endDate
    );
}
