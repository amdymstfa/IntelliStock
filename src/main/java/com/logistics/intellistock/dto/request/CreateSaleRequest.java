package com.logistics.intellistock.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSaleRequest {

  @NotNull(message = "Product ID is required")
  private Long productId;

  @NotNull(message = "Warehouse ID is required")
  private Long warehouseId;

  @NotNull(message = "Quantity sold is required")
  @Min(value = 1, message = "Quantity sold must be at least 1")
  private Integer quantitySold;

  private LocalDate saleDate;
}
