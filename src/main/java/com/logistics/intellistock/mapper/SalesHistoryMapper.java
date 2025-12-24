package com.logistics.intellistock.mapper;

import com.logistics.intellistock.dto.response.SalesHistoryResponse;
import com.logistics.intellistock.entity.SalesHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SalesHistoryMapper {

  @Mapping(target = "productId", source = "product.id")
  @Mapping(target = "productName", source = "product.name")
  @Mapping(target = "warehouseId", source = "warehouse.id")
  @Mapping(target = "warehouseName", source = "warehouse.name")
  SalesHistoryResponse toResponse(SalesHistory salesHistory);

  List<SalesHistoryResponse> toResponseList(List<SalesHistory> salesHistories);
}
