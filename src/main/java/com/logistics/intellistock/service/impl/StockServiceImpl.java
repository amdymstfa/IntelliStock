package com.logistics.intellistock.service.impl;

import com.logistics.intellistock.dto.request.UpdateStockRequest;
import com.logistics.intellistock.dto.response.StockResponse;
import com.logistics.intellistock.entity.Product;
import com.logistics.intellistock.entity.Stock;
import com.logistics.intellistock.entity.Warehouse;
import com.logistics.intellistock.exception.ResourceNotFoundException;
import com.logistics.intellistock.exception.StockException;
import com.logistics.intellistock.mapper.StockMapper;
import com.logistics.intellistock.repository.ProductRepository;
import com.logistics.intellistock.repository.StockRepository;
import com.logistics.intellistock.repository.WarehouseRepository;
import com.logistics.intellistock.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

  private final StockRepository stockRepository;
  private final ProductRepository productRepository;
  private final WarehouseRepository warehouseRepository;
  private final StockMapper stockMapper;

  @Override
  @Transactional(readOnly = true)
  public List<StockResponse> getStocksByWarehouse(Long warehouseId) {
    Warehouse warehouse = warehouseRepository.findById(warehouseId)
      .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", warehouseId));

    List<Stock> stocks = stockRepository.findByWarehouse(warehouse);
    return stockMapper.toResponseList(stocks);
  }

  @Override
  @Transactional(readOnly = true)
  public StockResponse getStockByProductAndWarehouse(Long productId, Long warehouseId) {
    Product product = productRepository.findById(productId)
      .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

    Warehouse warehouse = warehouseRepository.findById(warehouseId)
      .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", warehouseId));

    Stock stock = stockRepository.findByProductAndWarehouse(product, warehouse)
      .orElseThrow(() -> new ResourceNotFoundException(
        "Stock not found for product " + productId + " in warehouse " + warehouseId
      ));

    return stockMapper.toResponse(stock);
  }

  @Override
  @Transactional
  public StockResponse updateStock(Long productId, Long warehouseId, UpdateStockRequest request) {
    log.info("Updating stock for product {} in warehouse {}", productId, warehouseId);

    Product product = productRepository.findById(productId)
      .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

    Warehouse warehouse = warehouseRepository.findById(warehouseId)
      .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", warehouseId));

    // Find or create stock
    Stock stock = stockRepository.findByProductAndWarehouse(product, warehouse)
      .orElseGet(() -> Stock.builder()
        .product(product)
        .warehouse(warehouse)
        .quantityAvailable(0)
        .alertThreshold(10)
        .build());

    // Validate quantity
    if (request.getQuantityAvailable() < 0) {
      throw new StockException("Stock quantity cannot be negative");
    }

    // Update stock
    Integer oldQuantity = stock.getQuantityAvailable();
    stock.setQuantityAvailable(request.getQuantityAvailable());

    if (request.getAlertThreshold() != null) {
      stock.setAlertThreshold(request.getAlertThreshold());
    }

    // Update restock timestamp if quantity increased
    if (request.getQuantityAvailable() > oldQuantity) {
      stock.setLastRestockedAt(LocalDateTime.now());
    }

    Stock savedStock = stockRepository.save(stock);

    log.info("Stock updated successfully. Old: {}, New: {}", oldQuantity, savedStock.getQuantityAvailable());

    return stockMapper.toResponse(savedStock);
  }

  @Override
  @Transactional(readOnly = true)
  public List<StockResponse> getLowStockItems() {
    List<Stock> lowStocks = stockRepository.findLowStockItems();
    return stockMapper.toResponseList(lowStocks);
  }

  @Override
  @Transactional(readOnly = true)
  public List<StockResponse> getLowStockItemsByWarehouse(Long warehouseId) {
    Warehouse warehouse = warehouseRepository.findById(warehouseId)
      .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", warehouseId));

    List<Stock> lowStocks = stockRepository.findLowStockItemsByWarehouse(warehouse);
    return stockMapper.toResponseList(lowStocks);
  }
}
