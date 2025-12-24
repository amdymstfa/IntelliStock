package com.logistics.intellistock.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateWarehouseRequest {

  @NotBlank(message = "Warehouse name is required")
  @Size(max = 100, message = "Warehouse name must not exceed 100 characters")
  private String name;

  @NotBlank(message = "City is required")
  @Size(max = 100, message = "City must not exceed 100 characters")
  private String city;

  @NotBlank(message = "Address is required")
  @Size(max = 255, message = "Address must not exceed 255 characters")
  private String address;

  @NotBlank(message = "Country is required")
  @Size(max = 100, message = "Country must not exceed 100 characters")
  private String country;

  @Size(max = 20, message = "Postal code must not exceed 20 characters")
  private String postalCode;

  @Size(max = 20, message = "Phone must not exceed 20 characters")
  private String phone;

  @Email(message = "Invalid email format")
  @Size(max = 100, message = "Email must not exceed 100 characters")
  private String email;

  @Min(value = 0, message = "Capacity must be positive")
  private Integer capacity;
}
