package com.logistics.intellistock.mapper;

import com.logistics.intellistock.dto.response.WarehouseResponse;
import com.logistics.intellistock.entity.Warehouse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WarehouseMapper {

  WarehouseResponse toResponse(Warehouse warehouse);

  List<WarehouseResponse> toResponseList(List<Warehouse> warehouses);
}
