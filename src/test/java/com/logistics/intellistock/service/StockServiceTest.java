package com.logistics.intellistock.service;

import com.logistics.intellistock.dto.request.UpdateStockRequest;
import com.logistics.intellistock.dto.response.StockResponse;
import com.logistics.intellistock.entity.Product;
import com.logistics.intellistock.entity.Stock;
import com.logistics.intellistock.entity.Warehouse;
import com.logistics.intellistock.enums.Category;
import com.logistics.intellistock.enums.Unit;
import com.logistics.intellistock.exception.ResourceNotFoundException;
import com.logistics.intellistock.exception.StockException;
import com.logistics.intellistock.mapper.StockMapper;
import com.logistics.intellistock.repository.ProductRepository;
import com.logistics.intellistock.repository.StockRepository;
import com.logistics.intellistock.repository.WarehouseRepository;
import com.logistics.intellistock.service.impl.StockServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Stock Service Tests")
class StockServiceTest {

  @Mock
  private StockRepository stockRepository;

  @Mock
  private ProductRepository productRepository;

  @Mock
  private WarehouseRepository warehouseRepository;

  @Mock
  private StockMapper stockMapper;

  @InjectMocks
  private StockServiceImpl stockService;

  private Product testProduct;
  private Warehouse testWarehouse;
  private Stock testStock;
  private StockResponse stockResponse;
  private UpdateStockRequest updateRequest;

  @BeforeEach
  void setUp() {
    testProduct = Product.builder()
      .id(1L)
      .name("Test Laptop")
      .sku("ELEC-LAP-001")
      .category(Category.ELECTRONICS)
      .unit(Unit.UNIT)
      .isActive(true)
      .build();

    testWarehouse = Warehouse.builder()
      .id(1L)
      .name("New York Warehouse")
      .city("New York")
      .isActive(true)
      .build();

    testStock = Stock.builder()
      .id(1L)
      .product(testProduct)
      .warehouse(testWarehouse)
      .quantityAvailable(100)
      .alertThreshold(20)
      .lastRestockedAt(LocalDateTime.now())
      .build();

    stockResponse = StockResponse.builder()
      .id(1L)
      .productId(1L)
      .productName("Test Laptop")
      .productSku("ELEC-LAP-001")
      .warehouseId(1L)
      .warehouseName("New York Warehouse")
      .quantityAvailable(100)
      .alertThreshold(20)
      .isBelowThreshold(false)
      .build();

    updateRequest = new UpdateStockRequest(150, 25);
  }

  @Test
  @DisplayName("Should get stocks by warehouse successfully")
  void shouldGetStocksByWarehouseSuccessfully() {
    // Given
    when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));
    when(stockRepository.findByWarehouse(testWarehouse)).thenReturn(Arrays.asList(testStock));
    when(stockMapper.toResponseList(anyList())).thenReturn(Arrays.asList(stockResponse));

    // When
    List<StockResponse> result = stockService.getStocksByWarehouse(1L);

    // Then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getWarehouseId()).isEqualTo(1L);
    verify(warehouseRepository).findById(1L);
    verify(stockRepository).findByWarehouse(testWarehouse);
  }

  @Test
  @DisplayName("Should throw exception when warehouse not found")
  void shouldThrowExceptionWhenWarehouseNotFound() {
    // Given
    when(warehouseRepository.findById(999L)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> stockService.getStocksByWarehouse(999L))
      .isInstanceOf(ResourceNotFoundException.class)
      .hasMessageContaining("Warehouse");

    verify(warehouseRepository).findById(999L);
    verify(stockRepository, never()).findByWarehouse(any());
  }

  @Test
  @DisplayName("Should get stock by product and warehouse")
  void shouldGetStockByProductAndWarehouse() {
    // Given
    when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
    when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));
    when(stockRepository.findByProductAndWarehouse(testProduct, testWarehouse))
      .thenReturn(Optional.of(testStock));
    when(stockMapper.toResponse(testStock)).thenReturn(stockResponse);

    // When
    StockResponse result = stockService.getStockByProductAndWarehouse(1L, 1L);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getProductId()).isEqualTo(1L);
    assertThat(result.getWarehouseId()).isEqualTo(1L);
    verify(stockRepository).findByProductAndWarehouse(testProduct, testWarehouse);
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
    assertThatThrownBy(() -> stockService.getStockByProductAndWarehouse(1L, 1L))
      .isInstanceOf(ResourceNotFoundException.class)
      .hasMessageContaining("Stock not found");
  }

  @Test
  @DisplayName("Should update existing stock successfully")
  void shouldUpdateExistingStockSuccessfully() {
    // Given
    when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
    when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));
    when(stockRepository.findByProductAndWarehouse(testProduct, testWarehouse))
      .thenReturn(Optional.of(testStock));
    when(stockRepository.save(any(Stock.class))).thenReturn(testStock);
    when(stockMapper.toResponse(any(Stock.class))).thenReturn(stockResponse);

    // When
    StockResponse result = stockService.updateStock(1L, 1L, updateRequest);

    // Then
    assertThat(result).isNotNull();
    verify(stockRepository).save(argThat(stock ->
      stock.getQuantityAvailable().equals(150) &&
        stock.getAlertThreshold().equals(25)
    ));
  }

  @Test
  @DisplayName("Should create new stock if not exists")
  void shouldCreateNewStockIfNotExists() {
    // Given
    when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
    when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));
    when(stockRepository.findByProductAndWarehouse(testProduct, testWarehouse))
      .thenReturn(Optional.empty());
    when(stockRepository.save(any(Stock.class))).thenReturn(testStock);
    when(stockMapper.toResponse(any(Stock.class))).thenReturn(stockResponse);

    // When
    StockResponse result = stockService.updateStock(1L, 1L, updateRequest);

    // Then
    assertThat(result).isNotNull();
    verify(stockRepository).save(any(Stock.class));
  }

  @Test
  @DisplayName("Should throw exception for negative stock quantity")
  void shouldThrowExceptionForNegativeStockQuantity() {
    // Given
    UpdateStockRequest negativeRequest = new UpdateStockRequest(-10, 20);
    when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
    when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));
    when(stockRepository.findByProductAndWarehouse(testProduct, testWarehouse))
      .thenReturn(Optional.of(testStock));

    // When & Then
    assertThatThrownBy(() -> stockService.updateStock(1L, 1L, negativeRequest))
      .isInstanceOf(StockException.class)
      .hasMessageContaining("cannot be negative");

    verify(stockRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should update lastRestockedAt when quantity increases")
  void shouldUpdateLastRestockedAtWhenQuantityIncreases() {
    // Given
    testStock.setQuantityAvailable(50); // Old quantity
    UpdateStockRequest increaseRequest = new UpdateStockRequest(100, 20); // New quantity > old

    when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
    when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));
    when(stockRepository.findByProductAndWarehouse(testProduct, testWarehouse))
      .thenReturn(Optional.of(testStock));
    when(stockRepository.save(any(Stock.class))).thenReturn(testStock);
    when(stockMapper.toResponse(any(Stock.class))).thenReturn(stockResponse);

    // When
    stockService.updateStock(1L, 1L, increaseRequest);

    // Then
    verify(stockRepository).save(argThat(stock ->
      stock.getLastRestockedAt() != null
    ));
  }

  @Test
  @DisplayName("Should get low stock items")
  void shouldGetLowStockItems() {
    // Given
    Stock lowStock = Stock.builder()
      .id(2L)
      .product(testProduct)
      .warehouse(testWarehouse)
      .quantityAvailable(15)
      .alertThreshold(20)
      .build();

    when(stockRepository.findLowStockItems()).thenReturn(Arrays.asList(lowStock));
    when(stockMapper.toResponseList(anyList())).thenReturn(Arrays.asList(stockResponse));

    // When
    List<StockResponse> result = stockService.getLowStockItems();

    // Then
    assertThat(result).isNotEmpty();
    verify(stockRepository).findLowStockItems();
  }

  @Test
  @DisplayName("Should get low stock items by warehouse")
  void shouldGetLowStockItemsByWarehouse() {
    // Given
    when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));
    when(stockRepository.findLowStockItemsByWarehouse(testWarehouse))
      .thenReturn(Arrays.asList(testStock));
    when(stockMapper.toResponseList(anyList())).thenReturn(Arrays.asList(stockResponse));

    // When
    List<StockResponse> result = stockService.getLowStockItemsByWarehouse(1L);

    // Then
    assertThat(result).isNotEmpty();
    verify(stockRepository).findLowStockItemsByWarehouse(testWarehouse);
  }

  @Test
  @DisplayName("Should return empty list when no low stocks found")
  void shouldReturnEmptyListWhenNoLowStocksFound() {
    // Given
    when(stockRepository.findLowStockItems()).thenReturn(Arrays.asList());
    when(stockMapper.toResponseList(anyList())).thenReturn(Arrays.asList());

    // When
    List<StockResponse> result = stockService.getLowStockItems();

    // Then
    assertThat(result).isEmpty();
  }
}
