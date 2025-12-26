package com.logistics.intellistock.controller;

import com.logistics.intellistock.dto.request.UpdateStockRequest;
import com.logistics.intellistock.dto.response.ApiResponse;
import com.logistics.intellistock.dto.response.StockResponse;
import com.logistics.intellistock.security.SecurityUtils;
import com.logistics.intellistock.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stocks")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Stocks", description = "Stock management endpoints")
public class StockController {

  private final StockService stockService;
  private final SecurityUtils securityUtils;

  @GetMapping("/warehouse/{warehouseId}")
  @Operation(summary = "Get stocks by warehouse", description = "Get all stocks in a warehouse")
  public ResponseEntity<ApiResponse<List<StockResponse>>> getStocksByWarehouse(@PathVariable Long warehouseId) {
    // Check access rights
    securityUtils.validateWarehouseAccess(warehouseId);

    List<StockResponse> stocks = stockService.getStocksByWarehouse(warehouseId);
    return ResponseEntity.ok(ApiResponse.success(stocks));
  }

  @GetMapping("/product/{productId}/warehouse/{warehouseId}")
  @Operation(summary = "Get stock by product and warehouse", description = "Get stock for specific product in warehouse")
  public ResponseEntity<ApiResponse<StockResponse>> getStockByProductAndWarehouse(
    @PathVariable Long productId,
    @PathVariable Long warehouseId) {

    securityUtils.validateWarehouseAccess(warehouseId);

    StockResponse stock = stockService.getStockByProductAndWarehouse(productId, warehouseId);
    return ResponseEntity.ok(ApiResponse.success(stock));
  }

  @GetMapping("/low-stock")
  @Operation(summary = "Get low stock items", description = "Get all items below alert threshold")
  public ResponseEntity<ApiResponse<List<StockResponse>>> getLowStockItems() {
    List<StockResponse> lowStocks;

    if (securityUtils.isAdmin()) {
      lowStocks = stockService.getLowStockItems();
    } else {
      // Manager sees only their warehouse's low stocks
      Long warehouseId = securityUtils.getCurrentUser().getWarehouseId();
      lowStocks = stockService.getLowStockItemsByWarehouse(warehouseId);
    }

    return ResponseEntity.ok(ApiResponse.success(lowStocks));
  }

  @GetMapping("/low-stock/warehouse/{warehouseId}")
  @Operation(summary = "Get low stock items by warehouse", description = "Get low stock items in specific warehouse")
  public ResponseEntity<ApiResponse<List<StockResponse>>> getLowStockItemsByWarehouse(
    @PathVariable Long warehouseId) {

    securityUtils.validateWarehouseAccess(warehouseId);

    List<StockResponse> lowStocks = stockService.getLowStockItemsByWarehouse(warehouseId);
    return ResponseEntity.ok(ApiResponse.success(lowStocks));
  }

  @PutMapping("/product/{productId}/warehouse/{warehouseId}")
  @Operation(summary = "Update stock", description = "Update stock quantity and alert threshold")
  public ResponseEntity<ApiResponse<StockResponse>> updateStock(
    @PathVariable Long productId,
    @PathVariable Long warehouseId,
    @Valid @RequestBody UpdateStockRequest request) {

    securityUtils.validateWarehouseAccess(warehouseId);

    StockResponse stock = stockService.updateStock(productId, warehouseId, request);
    return ResponseEntity.ok(ApiResponse.success("Stock updated successfully", stock));
  }
}
