package com.logistics.intellistock.mapper;

import com.logistics.intellistock.dto.response.ProductAdminResponse;
import com.logistics.intellistock.dto.response.ProductResponse;
import com.logistics.intellistock.entity.Product;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

  ProductResponse toResponse(Product product);

  List<ProductResponse> toResponseList(List<Product> products);

  ProductAdminResponse toAdminResponse(Product product);

  List<ProductAdminResponse> toAdminResponseList(List<Product> products);
}
