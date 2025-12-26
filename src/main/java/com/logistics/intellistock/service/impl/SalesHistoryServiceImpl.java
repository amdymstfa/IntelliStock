package com.logistics.intellistock.service.impl;

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
import com.logistics.intellistock.service.SalesHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesHistoryServiceImpl implements SalesHistoryService {

  private final SalesHistoryRepository salesHistoryRepository;
  private final ProductRepository productRepository;
  private final WarehouseRepository warehouseRepository;
  private final StockRepository stockRepository;
  private final SalesHistoryMapper salesHistoryMapper;

  @Override
  @Transactional
  public SalesHistoryResponse recordSale(CreateSaleRequest request) {
    log.info("Recording sale for product {} in warehouse {}", request.getProductId(), request.getWarehouseId());

    Product product = productRepository.findById(request.getProductId())
      .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

    Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
      .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", request.getWarehouseId()));

    // Check stock availability
    Stock stock = stockRepository.findByProductAndWarehouse(product, warehouse)
      .orElseThrow(() -> new ResourceNotFoundException(
        "No stock found for this product in the specified warehouse"
      ));

    if (stock.getQuantityAvailable() < request.getQuantitySold()) {
      throw new StockException(
        "Insufficient stock. Available: " + stock.getQuantityAvailable() +
          ", Requested: " + request.getQuantitySold()
      );
    }

    // Use provided date or default to today
    LocalDate saleDate = request.getSaleDate() != null ? request.getSaleDate() : LocalDate.now();

    // Create sales record
    SalesHistory sale = SalesHistory.builder()
      .product(product)
      .warehouse(warehouse)
      .saleDate(saleDate)
      .quantitySold(request.getQuantitySold())
      .dayOfWeek(DayOfWeek.valueOf(saleDate.getDayOfWeek().name()))
      .month(saleDate.getMonthValue())
      .year(saleDate.getYear())
      .build();

    SalesHistory savedSale = salesHistoryRepository.save(sale);

    // Update stock
    stock.setQuantityAvailable(stock.getQuantityAvailable() - request.getQuantitySold());
    stockRepository.save(stock);

    log.info("Sale recorded successfully. Remaining stock: {}", stock.getQuantityAvailable());

    return salesHistoryMapper.toResponse(savedSale);
  }

  @Override
  @Transactional(readOnly = true)
  public List<SalesHistoryResponse> getSalesByWarehouse(Long warehouseId) {
    Warehouse warehouse = warehouseRepository.findById(warehouseId)
      .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", warehouseId));

    List<SalesHistory> sales = salesHistoryRepository.findByWarehouse(warehouse);
    return salesHistoryMapper.toResponseList(sales);
  }

  @Override
  @Transactional(readOnly = true)
  public List<SalesHistoryResponse> getSalesByProduct(Long productId) {
    Product product = productRepository.findById(productId)
      .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

    List<SalesHistory> sales = salesHistoryRepository.findByProduct(product);
    return salesHistoryMapper.toResponseList(sales);
  }

  @Override
  @Transactional(readOnly = true)
  public List<SalesHistoryResponse> getSalesByWarehouseBetweenDates(
    Long warehouseId,
    LocalDate startDate,
    LocalDate endDate
  ) {
    Warehouse warehouse = warehouseRepository.findById(warehouseId)
      .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", warehouseId));

    List<SalesHistory> sales = salesHistoryRepository.findByWarehouseBetweenDates(
      warehouse,
      startDate,
      endDate
    );

    return salesHistoryMapper.toResponseList(sales);
  }
}
