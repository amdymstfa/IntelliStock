package com.logistics.intellistock.service;

import com.logistics.intellistock.dto.request.CreateProductRequest;
import com.logistics.intellistock.dto.request.UpdateProductRequest;
import com.logistics.intellistock.dto.response.ProductAdminResponse;
import com.logistics.intellistock.dto.response.ProductResponse;
import com.logistics.intellistock.entity.Product;
import com.logistics.intellistock.enums.Category;
import com.logistics.intellistock.enums.Unit;
import com.logistics.intellistock.exception.DuplicateResourceException;
import com.logistics.intellistock.exception.ResourceNotFoundException;
import com.logistics.intellistock.mapper.ProductMapper;
import com.logistics.intellistock.repository.ProductRepository;
import com.logistics.intellistock.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Product Service Tests")
class ProductServiceTest {

  @Mock
  private ProductRepository productRepository;

  @Mock
  private EncryptionService encryptionService;

  @Mock
  private ProductMapper productMapper;

  @InjectMocks
  private ProductServiceImpl productService;

  private Product testProduct;
  private CreateProductRequest createRequest;
  private UpdateProductRequest updateRequest;
  private ProductResponse productResponse;
  private ProductAdminResponse adminResponse;

  @BeforeEach
  void setUp() {
    testProduct = Product.builder()
      .id(1L)
      .name("Test Laptop")
      .description("High-performance laptop")
      .category(Category.ELECTRONICS)
      .sku("ELEC-LAP-001")
      .sellingPrice(new BigDecimal("1500.00"))
      .purchasePriceEncrypted("encrypted_1200")
      .marginEncrypted("encrypted_300")
      .weight(new BigDecimal("2.5"))
      .unit(Unit.UNIT)
      .isActive(true)
      .build();

    createRequest = new CreateProductRequest(
      "Test Laptop",
      "High-performance laptop",
      Category.ELECTRONICS,
      "ELEC-LAP-001",
      new BigDecimal("1500.00"),
      new BigDecimal("1200.00"),
      new BigDecimal("2.5"),
      Unit.UNIT
    );

    updateRequest = new UpdateProductRequest();
    updateRequest.setName("Updated Laptop");
    updateRequest.setSellingPrice(new BigDecimal("1600.00"));

    productResponse = ProductResponse.builder()
      .id(1L)
      .name("Test Laptop")
      .sku("ELEC-LAP-001")
      .category(Category.ELECTRONICS)
      .sellingPrice(new BigDecimal("1500.00"))
      .build();

    adminResponse = ProductAdminResponse.builder()
      .id(1L)
      .name("Test Laptop")
      .sku("ELEC-LAP-001")
      .sellingPrice(new BigDecimal("1500.00"))
      .purchasePrice(new BigDecimal("1200.00"))
      .margin(new BigDecimal("300.00"))
      .build();
  }

  @Test
  @DisplayName("Should create product successfully")
  void shouldCreateProductSuccessfully() throws Exception {
    when(productRepository.existsBySku(createRequest.getSku())).thenReturn(false);
    when(productRepository.save(any(Product.class))).thenReturn(testProduct);
    when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);
    when(encryptionService.encrypt(anyString())).thenReturn("encrypted_value");

    ProductResponse result = productService.createProduct(createRequest);

    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("Test Laptop");
    assertThat(result.getSku()).isEqualTo("ELEC-LAP-001");

    verify(encryptionService, times(2)).encrypt(anyString());
  }

  @Test
  @DisplayName("Should throw exception when creating product with duplicate SKU")
  void shouldThrowExceptionWhenCreatingProductWithDuplicateSku() {
    when(productRepository.existsBySku(createRequest.getSku())).thenReturn(true);

    assertThatThrownBy(() -> productService.createProduct(createRequest))
      .isInstanceOf(DuplicateResourceException.class);

    verify(productRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should get product by ID successfully")
  void shouldGetProductByIdSuccessfully() {
    when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
    when(productMapper.toResponse(testProduct)).thenReturn(productResponse);

    ProductResponse result = productService.getProductById(1L);

    assertThat(result.getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("Should throw exception when product not found")
  void shouldThrowExceptionWhenProductNotFound() {
    when(productRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> productService.getProductById(99L))
      .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("Should get all active products")
  void shouldGetAllActiveProducts() {
    when(productRepository.findByIsActive(true)).thenReturn(List.of(testProduct));
    when(productMapper.toResponseList(anyList())).thenReturn(List.of(productResponse));

    List<ProductResponse> result = productService.getAllProducts();

    assertThat(result).hasSize(1);
  }

  @Test
  @DisplayName("Should get products by category")
  void shouldGetProductsByCategory() {
    when(productRepository.findByCategory(Category.ELECTRONICS)).thenReturn(List.of(testProduct));
    when(productMapper.toResponseList(anyList())).thenReturn(List.of(productResponse));

    List<ProductResponse> result = productService.getProductsByCategory(Category.ELECTRONICS);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getCategory()).isEqualTo(Category.ELECTRONICS);
  }

  @Test
  @DisplayName("Should update product successfully")
  void shouldUpdateProductSuccessfully() throws Exception {
    when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
    when(productRepository.save(any(Product.class))).thenReturn(testProduct);
    when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);

    ProductResponse result = productService.updateProduct(1L, updateRequest);

    assertThat(result).isNotNull();
  }

  @Test
  @DisplayName("Should delete product softly")
  void shouldDeleteProductSoftly() {
    when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
    when(productRepository.save(any(Product.class))).thenReturn(testProduct);

    productService.deleteProduct(1L);

    verify(productRepository).save(argThat(p -> !p.getIsActive()));
  }

  @Test
  @DisplayName("Should get admin product response with decrypted prices")
  void shouldGetAdminProductResponseWithDecryptedPrices() throws Exception {
    when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
    when(productMapper.toAdminResponse(testProduct)).thenReturn(adminResponse);
    when(encryptionService.decrypt("encrypted_1200")).thenReturn("1200.00");
    when(encryptionService.decrypt("encrypted_300")).thenReturn("300.00");

    ProductAdminResponse result = productService.getProductByIdAdmin(1L);

    assertThat(result.getPurchasePrice()).isEqualTo(new BigDecimal("1200.00"));
    assertThat(result.getMargin()).isEqualTo(new BigDecimal("300.00"));
    verify(encryptionService, times(2)).decrypt(anyString());
  }

  @Test
  @DisplayName("Should return empty list when no products found")
  void shouldReturnEmptyListWhenNoProductsFound() {
    when(productRepository.findByIsActive(true)).thenReturn(List.of());
    when(productMapper.toResponseList(anyList())).thenReturn(List.of());

    List<ProductResponse> result = productService.getAllProducts();

    assertThat(result).isEmpty();
  }
}
