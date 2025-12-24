package com.logistics.intellistock.controller;

import com.logistics.intellistock.dto.request.CreateWarehouseRequest;
import com.logistics.intellistock.dto.request.UpdateWarehouseRequest;
import com.logistics.intellistock.dto.response.ApiResponse;
import com.logistics.intellistock.dto.response.WarehouseResponse;
import com.logistics.intellistock.security.SecurityUtils;
import com.logistics.intellistock.service.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Warehouses", description = "Warehouse management endpoints")
public class WarehouseController {

  private final WarehouseService warehouseService;
  private final SecurityUtils securityUtils;

  @GetMapping
  @Operation(summary = "Get all warehouses", description = "Get list of all warehouses (ADMIN sees all, MANAGER sees only theirs)")
  public ResponseEntity<ApiResponse<List<WarehouseResponse>>> getAllWarehouses() {
    List<WarehouseResponse> warehouses;

    if (securityUtils.isAdmin()) {
      warehouses = warehouseService.getAllWarehouses();
    } else {
      // Manager can only see their assigned warehouse
      Long warehouseId = securityUtils.getCurrentUser().getWarehouse().getId();
      WarehouseResponse warehouse = warehouseService.getWarehouseById(warehouseId);
      warehouses = List.of(warehouse);
    }

    return ResponseEntity.ok(ApiResponse.success(warehouses));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get warehouse by ID", description = "Get warehouse details by ID")
  public ResponseEntity<ApiResponse<WarehouseResponse>> getWarehouseById(@PathVariable Long id) {
    // Check access rights
    securityUtils.validateWarehouseAccess(id);

    WarehouseResponse warehouse = warehouseService.getWarehouseById(id);
    return ResponseEntity.ok(ApiResponse.success(warehouse));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Create warehouse", description = "Create a new warehouse (ADMIN only)")
  public ResponseEntity<ApiResponse<WarehouseResponse>> createWarehouse(
    @Valid @RequestBody CreateWarehouseRequest request) {
    WarehouseResponse warehouse = warehouseService.createWarehouse(request);
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(ApiResponse.success("Warehouse created successfully", warehouse));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Update warehouse", description = "Update warehouse details (ADMIN only)")
  public ResponseEntity<ApiResponse<WarehouseResponse>> updateWarehouse(
    @PathVariable Long id,
    @Valid @RequestBody UpdateWarehouseRequest request) {
    WarehouseResponse warehouse = warehouseService.updateWarehouse(id, request);
    return ResponseEntity.ok(ApiResponse.success("Warehouse updated successfully", warehouse));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Delete warehouse", description = "Delete warehouse (soft delete, ADMIN only)")
  public ResponseEntity<ApiResponse<Void>> deleteWarehouse(@PathVariable Long id) {
    warehouseService.deleteWarehouse(id);
    return ResponseEntity.ok(ApiResponse.success("Warehouse deleted successfully", null));
  }
}
