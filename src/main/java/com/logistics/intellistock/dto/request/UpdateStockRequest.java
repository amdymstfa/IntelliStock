package com.logistics.intellistock.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStockRequest {

  @NotNull(message = "Quantity is required")
  @Min(value = 0, message = "Quantity must be positive or zero")
  private Integer quantityAvailable;

  @Min(value = 0, message = "Alert threshold must be positive or zero")
  private Integer alertThreshold;
}
