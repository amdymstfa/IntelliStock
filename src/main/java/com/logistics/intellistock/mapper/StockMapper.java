package com.logistics.intellistock.mapper;

import com.logistics.intellistock.dto.response.StockResponse;
import com.logistics.intellistock.entity.Stock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StockMapper {

  @Mapping(target = "productId", source = "product.id")
  @Mapping(target = "productName", source = "product.name")
  @Mapping(target = "productSku", source = "product.sku")
  @Mapping(target = "warehouseId", source = "warehouse.id")
  @Mapping(target = "warehouseName", source = "warehouse.name")
  @Mapping(target = "isBelowThreshold", expression = "java(stock.getQuantityAvailable() <= stock.getAlertThreshold())")
  StockResponse toResponse(Stock stock);

  List<StockResponse> toResponseList(List<Stock> stocks);
}
