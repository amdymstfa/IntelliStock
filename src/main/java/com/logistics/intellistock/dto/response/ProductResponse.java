package com.logistics.intellistock.dto.response;

import com.logistics.intellistock.enums.Category;
import com.logistics.intellistock.enums.Unit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private Category category;
    private String sku;
    private BigDecimal sellingPrice;
    private BigDecimal weight;
    private Unit unit;
    private Boolean isActive;
}
