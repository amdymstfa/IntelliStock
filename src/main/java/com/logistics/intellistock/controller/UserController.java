package com.logistics.intellistock.controller;

import com.logistics.intellistock.dto.request.AssignManagerRequest;
import com.logistics.intellistock.dto.response.ApiResponse;
import com.logistics.intellistock.dto.response.UserResponse;
import com.logistics.intellistock.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Users", description = "User management endpoints (ADMIN only)")
public class UserController {

  private final UserService userService;

  @GetMapping
  @Operation(summary = "Get all users", description = "Get list of all users (ADMIN only)")
  public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
    List<UserResponse> users = userService.getAllUsers();
    return ResponseEntity.ok(ApiResponse.success(users));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get user by ID", description = "Get user details by ID (ADMIN only)")
  public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
    UserResponse user = userService.getUserById(id);
    return ResponseEntity.ok(ApiResponse.success(user));
  }

  @GetMapping("/warehouse/{warehouseId}")
  @Operation(summary = "Get users by warehouse", description = "Get all users assigned to a warehouse (ADMIN only)")
  public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByWarehouse(@PathVariable Long warehouseId) {
    List<UserResponse> users = userService.getUsersByWarehouse(warehouseId);
    return ResponseEntity.ok(ApiResponse.success(users));
  }

  @PostMapping("/assign-warehouse")
  @Operation(summary = "Assign warehouse to manager", description = "Assign a warehouse to a manager (ADMIN only)")
  public ResponseEntity<ApiResponse<Void>> assignWarehouse(@Valid @RequestBody AssignManagerRequest request) {
    userService.assignWarehouse(request.getUserId(), request.getWarehouseId());
    return ResponseEntity.ok(ApiResponse.success("Warehouse assigned successfully", null));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete user", description = "Delete user (deactivate, ADMIN only)")
  public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
    userService.deleteUser(id);
    return ResponseEntity.ok(ApiResponse.success("User deactivated successfully", null));
  }
}
