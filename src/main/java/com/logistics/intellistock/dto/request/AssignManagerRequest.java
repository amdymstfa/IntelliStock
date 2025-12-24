package com.logistics.intellistock.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignManagerRequest {

  @NotNull(message = "User ID is required")
  private Long userId;

  @NotNull(message = "Warehouse ID is required")
  private Long warehouseId;
}
