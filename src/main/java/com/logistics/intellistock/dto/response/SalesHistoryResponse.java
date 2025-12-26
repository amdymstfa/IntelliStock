package com.logistics.intellistock.dto.response;

import com.logistics.intellistock.entity.enums.DayOfWeek;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesHistoryResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Long warehouseId;
    private String warehouseName;
    private LocalDate saleDate;
    private Integer quantitySold;
    private DayOfWeek dayOfWeek;
    private Integer month;
    private Integer year;
}
