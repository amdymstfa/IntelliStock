package com.logistics.intellistock.controller;

import com.logistics.intellistock.dto.request.CreateProductRequest;
import com.logistics.intellistock.dto.request.UpdateProductRequest;
import com.logistics.intellistock.dto.response.ApiResponse;
import com.logistics.intellistock.dto.response.ProductAdminResponse;
import com.logistics.intellistock.dto.response.ProductResponse;
import com.logistics.intellistock.entity.enums.Category;
import com.logistics.intellistock.security.SecurityUtils;
import com.logistics.intellistock.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

  import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Products", description = "Product management endpoints")
public class ProductController {

  private final ProductService productService;
  private final SecurityUtils securityUtils;

  @GetMapping
  @Operation(summary = "Get all products", description = "Get list of all active products")
  public ResponseEntity<ApiResponse<List<?>>> getAllProducts() {
    if (securityUtils.isAdmin()) {
      List<ProductAdminResponse> products = productService.getAllProductsAdmin();
      return ResponseEntity.ok(ApiResponse.success(products));
    } else {
      List<ProductResponse> products = productService.getAllProducts();
      return ResponseEntity.ok(ApiResponse.success(products));
    }
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get product by ID", description = "Get product details by ID")
  public ResponseEntity<ApiResponse<?>> getProductById(@PathVariable Long id) {
    if (securityUtils.isAdmin()) {
      ProductAdminResponse product = productService.getProductByIdAdmin(id);
      return ResponseEntity.ok(ApiResponse.success(product));
    } else {
      ProductResponse product = productService.getProductById(id);
      return ResponseEntity.ok(ApiResponse.success(product));
    }
  }

  @GetMapping("/category/{category}")
  @Operation(summary = "Get products by category", description = "Get all products in a specific category")
  public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductsByCategory(
    @PathVariable Category category) {
    List<ProductResponse> products = productService.getProductsByCategory(category);
    return ResponseEntity.ok(ApiResponse.success(products));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Create product", description = "Create a new product (ADMIN only)")
  public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
    @Valid @RequestBody CreateProductRequest request) {
    ProductResponse product = productService.createProduct(request);
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(ApiResponse.success("Product created successfully", product));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Update product", description = "Update product details (ADMIN only)")
  public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
    @PathVariable Long id,
    @Valid @RequestBody UpdateProductRequest request) {
    ProductResponse product = productService.updateProduct(id, request);
    return ResponseEntity.ok(ApiResponse.success("Product updated successfully", product));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Delete product", description = "Delete product (soft delete, ADMIN only)")
  public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
    productService.deleteProduct(id);
    return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", null));
  }
}

