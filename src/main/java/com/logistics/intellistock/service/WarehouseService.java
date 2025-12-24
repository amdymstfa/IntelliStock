package com.logistics.intellistock.service;

import com.logistics.intellistock.dto.request.CreateWarehouseRequest;
import com.logistics.intellistock.dto.request.UpdateWarehouseRequest;
import com.logistics.intellistock.dto.response.WarehouseResponse;

import java.util.List;

public interface WarehouseService {
    WarehouseResponse createWarehouse(CreateWarehouseRequest request);
    WarehouseResponse getWarehouseById(Long id);
    List<WarehouseResponse> getAllWarehouses();
    WarehouseResponse updateWarehouse(Long id, UpdateWarehouseRequest request);
    void deleteWarehouse(Long id);
}
