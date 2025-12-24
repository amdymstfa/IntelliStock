package com.logistics.intellistock.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseResponse {
    private Long id;
    private String name;
    private String city;
    private String address;
    private String country;
    private String postalCode;
    private String phone;
    private String email;
    private Integer capacity;
    private Boolean isActive;
}
