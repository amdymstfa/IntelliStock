package com.logistics.intellistock.dto.response;

import com.logistics.intellistock.entity.enums.Category;
import com.logistics.intellistock.entity.enums.Unit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductAdminResponse {
    private Long id;
    private String name;
    private String description;
    private Category category;
    private String sku;
    private BigDecimal sellingPrice;
    private BigDecimal purchasePrice;
    private BigDecimal margin;
    private BigDecimal weight;
    private Unit unit;
    private Boolean isActive;
}
