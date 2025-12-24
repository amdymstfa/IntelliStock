package com.logistics.intellistock.controller;

import com.logistics.intellistock.dto.request.CreateSaleRequest;
import com.logistics.intellistock.dto.response.ApiResponse;
import com.logistics.intellistock.dto.response.SalesHistoryResponse;
import com.logistics.intellistock.security.SecurityUtils;
import com.logistics.intellistock.service.SalesHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Sales History", description = "Sales history management endpoints")
public class SalesHistoryController {

  private final SalesHistoryService salesHistoryService;
  private final SecurityUtils securityUtils;

  @PostMapping
  @Operation(summary = "Record sale", description = "Record a new sale and update stock")
  public ResponseEntity<ApiResponse<SalesHistoryResponse>> recordSale(
    @Valid @RequestBody CreateSaleRequest request) {

    securityUtils.validateWarehouseAccess(request.getWarehouseId());

    SalesHistoryResponse sale = salesHistoryService.recordSale(request);
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(ApiResponse.success("Sale recorded successfully", sale));
  }

  @GetMapping("/warehouse/{warehouseId}")
  @Operation(summary = "Get sales by warehouse", description = "Get all sales for a warehouse")
  public ResponseEntity<ApiResponse<List<SalesHistoryResponse>>> getSalesByWarehouse(
    @PathVariable Long warehouseId) {

    securityUtils.validateWarehouseAccess(warehouseId);

    List<SalesHistoryResponse> sales = salesHistoryService.getSalesByWarehouse(warehouseId);
    return ResponseEntity.ok(ApiResponse.success(sales));
  }

  @GetMapping("/product/{productId}")
  @Operation(summary = "Get sales by product", description = "Get all sales for a specific product")
  public ResponseEntity<ApiResponse<List<SalesHistoryResponse>>> getSalesByProduct(
    @PathVariable Long productId) {

    List<SalesHistoryResponse> sales = salesHistoryService.getSalesByProduct(productId);
    return ResponseEntity.ok(ApiResponse.success(sales));
  }

  @GetMapping("/warehouse/{warehouseId}/period")
  @Operation(summary = "Get sales by warehouse and date range", description = "Get sales for warehouse within date range")
  public ResponseEntity<ApiResponse<List<SalesHistoryResponse>>> getSalesByWarehouseBetweenDates(
    @PathVariable Long warehouseId,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

    securityUtils.validateWarehouseAccess(warehouseId);

    List<SalesHistoryResponse> sales = salesHistoryService.getSalesByWarehouseBetweenDates(
      warehouseId,
      startDate,
      endDate
    );

    return ResponseEntity.ok(ApiResponse.success(sales));
  }
}
