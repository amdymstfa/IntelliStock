package com.logistics.intellistock.service;

import com.logistics.intellistock.dto.request.CreateSaleRequest;
import com.logistics.intellistock.dto.response.SalesHistoryResponse;
import com.logistics.intellistock.entity.Product;
import com.logistics.intellistock.entity.SalesHistory;
import com.logistics.intellistock.entity.Stock;
import com.logistics.intellistock.entity.Warehouse;
import com.logistics.intellistock.entity.enums.DayOfWeek;
import com.logistics.intellistock.core.exception.ResourceNotFoundException;
import com.logistics.intellistock.core.exception.StockException;
import com.logistics.intellistock.mapper.SalesHistoryMapper;
import com.logistics.intellistock.repository.ProductRepository;
import com.logistics.intellistock.repository.SalesHistoryRepository;
import com.logistics.intellistock.repository.StockRepository;
import com.logistics.intellistock.repository.WarehouseRepository;
import com.logistics.intellistock.service.impl.SalesHistoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Sales History Service Tests")
class SalesHistoryServiceTest {

  @Mock
  private SalesHistoryRepository salesHistoryRepository;

  @Mock
  private ProductRepository productRepository;

  @Mock
  private WarehouseRepository warehouseRepository;

  @Mock
  private StockRepository stockRepository;

  @Mock
  private SalesHistoryMapper salesHistoryMapper;

  @InjectMocks
  private SalesHistoryServiceImpl salesHistoryService;

  private Product testProduct;
  private Warehouse testWarehouse;
  private Stock testStock;
  private SalesHistory testSale;
  private CreateSaleRequest createRequest;
  private SalesHistoryResponse salesResponse;

  @BeforeEach
  void setUp() {
    testProduct = Product.builder()
      .id(1L)
      .name("Test Product")
      .sku("TEST-001")
      .build();

    testWarehouse = Warehouse.builder()
      .id(1L)
      .name("Test Warehouse")
      .build();

    testStock = Stock.builder()
      .id(1L)
      .product(testProduct)
      .warehouse(testWarehouse)
      .quantityAvailable(100)
      .alertThreshold(20)
      .build();

    createRequest = new CreateSaleRequest(
      1L,  // productId
      1L,  // warehouseId
      10,  // quantitySold
      LocalDate.now()
    );

    testSale = SalesHistory.builder()
      .id(1L)
      .product(testProduct)
      .warehouse(testWarehouse)
      .saleDate(LocalDate.now())
      .quantitySold(10)
      .dayOfWeek(DayOfWeek.MONDAY)
      .month(LocalDate.now().getMonthValue())
      .year(LocalDate.now().getYear())
      .build();

    salesResponse = SalesHistoryResponse.builder()
      .id(1L)
      .productId(1L)
      .productName("Test Product")
      .warehouseId(1L)
      .warehouseName("Test Warehouse")
      .quantitySold(10)
      .saleDate(LocalDate.now())
      .build();
  }

  @Test
  @DisplayName("Should record sale successfully")
  void shouldRecordSaleSuccessfully() {
    // Given
    when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
    when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));
    when(stockRepository.findByProductAndWarehouse(testProduct, testWarehouse))
      .thenReturn(Optional.of(testStock));
    when(salesHistoryRepository.save(any(SalesHistory.class))).thenReturn(testSale);
    when(stockRepository.save(any(Stock.class))).thenReturn(testStock);
    when(salesHistoryMapper.toResponse(any(SalesHistory.class))).thenReturn(salesResponse);

    // When
    SalesHistoryResponse result = salesHistoryService.recordSale(createRequest);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getQuantitySold()).isEqualTo(10);

    verify(salesHistoryRepository).save(any(SalesHistory.class));
    verify(stockRepository).save(argThat(stock ->
      stock.getQuantityAvailable() == 90 // 100 - 10
    ));
  }

  @Test
  @DisplayName("Should throw exception when product not found")
  void shouldThrowExceptionWhenProductNotFound() {
    // Given
    when(productRepository.findById(999L)).thenReturn(Optional.empty());

    CreateSaleRequest invalidRequest = new CreateSaleRequest(999L, 1L, 10, LocalDate.now());

    // When & Then
    assertThatThrownBy(() -> salesHistoryService.recordSale(invalidRequest))
      .isInstanceOf(ResourceNotFoundException.class)
      .hasMessageContaining("Product");

    verify(salesHistoryRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw exception when warehouse not found")
  void shouldThrowExceptionWhenWarehouseNotFound() {
    // Given
    when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
    when(warehouseRepository.findById(999L)).thenReturn(Optional.empty());

    CreateSaleRequest invalidRequest = new CreateSaleRequest(1L, 999L, 10, LocalDate.now());

    // When & Then
    assertThatThrownBy(() -> salesHistoryService.recordSale(invalidRequest))
      .isInstanceOf(ResourceNotFoundException.class)
      .hasMessageContaining("Warehouse");

    verify(salesHistoryRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw exception when stock not found")
  void shouldThrowExceptionWhenStockNotFound() {
    // Given
    when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
    when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));
    when(stockRepository.findByProductAndWarehouse(testProduct, testWarehouse))
      .thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> salesHistoryService.recordSale(createRequest))
      .isInstanceOf(ResourceNotFoundException.class)
      .hasMessageContaining("No stock found");

    verify(salesHistoryRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw exception when insufficient stock")
  void shouldThrowExceptionWhenInsufficientStock() {
    // Given
    testStock.setQuantityAvailable(5); // Less than requested
    CreateSaleRequest largeRequest = new CreateSaleRequest(1L, 1L, 10, LocalDate.now());

    when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
    when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));
    when(stockRepository.findByProductAndWarehouse(testProduct, testWarehouse))
      .thenReturn(Optional.of(testStock));

    // When & Then
    assertThatThrownBy(() -> salesHistoryService.recordSale(largeRequest))
      .isInstanceOf(StockException.class)
      .hasMessageContaining("Insufficient stock");

    verify(salesHistoryRepository, never()).save(any());
    verify(stockRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should use current date when sale date not provided")
  void shouldUseCurrentDateWhenSaleDateNotProvided() {
    // Given
    CreateSaleRequest requestWithoutDate = new CreateSaleRequest(1L, 1L, 10, null);

    when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
    when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));
    when(stockRepository.findByProductAndWarehouse(testProduct, testWarehouse))
      .thenReturn(Optional.of(testStock));
    when(salesHistoryRepository.save(any(SalesHistory.class))).thenReturn(testSale);
    when(stockRepository.save(any(Stock.class))).thenReturn(testStock);
    when(salesHistoryMapper.toResponse(any(SalesHistory.class))).thenReturn(salesResponse);

    // When
    SalesHistoryResponse result = salesHistoryService.recordSale(requestWithoutDate);

    // Then
    assertThat(result).isNotNull();
    verify(salesHistoryRepository).save(argThat(sale ->
      sale.getSaleDate().equals(LocalDate.now())
    ));
  }

  @Test
  @DisplayName("Should get sales by warehouse")
  void shouldGetSalesByWarehouse() {
    // Given
    when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));
    when(salesHistoryRepository.findByWarehouse(testWarehouse))
      .thenReturn(Arrays.asList(testSale));
    when(salesHistoryMapper.toResponseList(anyList()))
      .thenReturn(Arrays.asList(salesResponse));

    // When
    List<SalesHistoryResponse> result = salesHistoryService.getSalesByWarehouse(1L);

    // Then
    assertThat(result).hasSize(1);
    verify(salesHistoryRepository).findByWarehouse(testWarehouse);
  }

  @Test
  @DisplayName("Should get sales by product")
  void shouldGetSalesByProduct() {
    // Given
    when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
    when(salesHistoryRepository.findByProduct(testProduct))
      .thenReturn(Arrays.asList(testSale));
    when(salesHistoryMapper.toResponseList(anyList()))
      .thenReturn(Arrays.asList(salesResponse));

    // When
    List<SalesHistoryResponse> result = salesHistoryService.getSalesByProduct(1L);

    // Then
    assertThat(result).hasSize(1);
    verify(salesHistoryRepository).findByProduct(testProduct);
  }

  @Test
  @DisplayName("Should get sales by warehouse between dates")
  void shouldGetSalesByWarehouseBetweenDates() {
    // Given
    LocalDate startDate = LocalDate.now().minusDays(30);
    LocalDate endDate = LocalDate.now();

    when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));
    when(salesHistoryRepository.findByWarehouseBetweenDates(testWarehouse, startDate, endDate))
      .thenReturn(Arrays.asList(testSale));
    when(salesHistoryMapper.toResponseList(anyList()))
      .thenReturn(Arrays.asList(salesResponse));

    // When
    List<SalesHistoryResponse> result = salesHistoryService.getSalesByWarehouseBetweenDates(
      1L, startDate, endDate
    );

    // Then
    assertThat(result).hasSize(1);
    verify(salesHistoryRepository).findByWarehouseBetweenDates(testWarehouse, startDate, endDate);
  }

  @Test
  @DisplayName("Should correctly set day of week, month and year")
  void shouldCorrectlySetDateFields() {
    // Given
    LocalDate specificDate = LocalDate.of(2024, 12, 25); // Wednesday
    CreateSaleRequest request = new CreateSaleRequest(1L, 1L, 10, specificDate);

    when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
    when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));
    when(stockRepository.findByProductAndWarehouse(testProduct, testWarehouse))
      .thenReturn(Optional.of(testStock));
    when(salesHistoryRepository.save(any(SalesHistory.class))).thenReturn(testSale);
    when(stockRepository.save(any(Stock.class))).thenReturn(testStock);
    when(salesHistoryMapper.toResponse(any(SalesHistory.class))).thenReturn(salesResponse);

    // When
    salesHistoryService.recordSale(request);

    // Then
    verify(salesHistoryRepository).save(argThat(sale ->
      sale.getSaleDate().equals(specificDate) &&
        sale.getMonth() == 12 &&
        sale.getYear() == 2024 &&
        sale.getDayOfWeek() == DayOfWeek.WEDNESDAY
    ));
  }

  @Test
  @DisplayName("Should return empty list when no sales found")
  void shouldReturnEmptyListWhenNoSalesFound() {
    // Given
    when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));
    when(salesHistoryRepository.findByWarehouse(testWarehouse)).thenReturn(Arrays.asList());
    when(salesHistoryMapper.toResponseList(anyList())).thenReturn(Arrays.asList());

    // When
    List<SalesHistoryResponse> result = salesHistoryService.getSalesByWarehouse(1L);

    // Then
    assertThat(result).isEmpty();
  }
}
