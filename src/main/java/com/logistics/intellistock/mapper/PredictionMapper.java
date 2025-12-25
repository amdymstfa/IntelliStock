package com.logistics.intellistock.mapper;

import com.logistics.intellistock.dto.response.PredictionResponse;
import com.logistics.intellistock.entity.Prediction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PredictionMapper {

  @Mapping(target = "productId", source = "product.id")
  @Mapping(target = "productName", source = "product.name")
  @Mapping(target = "productSku", source = "product.sku")
  @Mapping(target = "warehouseId", source = "warehouse.id")
  @Mapping(target = "warehouseName", source = "warehouse.name")
  @Mapping(target = "currentStock", ignore = true)
  @Mapping(target = "alertThreshold", ignore = true)
  PredictionResponse toResponse(Prediction prediction);

  List<PredictionResponse> toResponseList(List<Prediction> predictions);
}
