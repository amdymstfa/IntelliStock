package com.logistics.intellistock.service;

import com.logistics.intellistock.dto.request.CreateProductRequest;
import com.logistics.intellistock.dto.request.UpdateProductRequest;
import com.logistics.intellistock.dto.response.ProductAdminResponse;
import com.logistics.intellistock.dto.response.ProductResponse;
import com.logistics.intellistock.entity.enums.Category;

import java.util.List;

public interface ProductService {
    ProductResponse createProduct(CreateProductRequest request);
    ProductResponse getProductById(Long id);
    ProductAdminResponse getProductByIdAdmin(Long id);
    List<ProductResponse> getAllProducts();
    List<ProductAdminResponse> getAllProductsAdmin();
    List<ProductResponse> getProductsByCategory(Category category);
    ProductResponse updateProduct(Long id, UpdateProductRequest request);
    void deleteProduct(Long id);
}
