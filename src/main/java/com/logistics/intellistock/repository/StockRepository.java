package com.logistics.intellistock.repository;

import com.logistics.intellistock.entity.Product;
import com.logistics.intellistock.entity.Stock;
import com.logistics.intellistock.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

  Optional<Stock> findByProductAndWarehouse(Product product, Warehouse warehouse);

  List<Stock> findByWarehouse(Warehouse warehouse);

  List<Stock> findByProduct(Product product);

  @Query("SELECT s FROM Stock s WHERE s.quantityAvailable <= s.alertThreshold")
  List<Stock> findLowStockItems();

  @Query("SELECT s FROM Stock s WHERE s.warehouse = :warehouse AND s.quantityAvailable <= s.alertThreshold")
  List<Stock> findLowStockItemsByWarehouse(@Param("warehouse") Warehouse warehouse);

  @Query("SELECT s FROM Stock s WHERE s.warehouse.id = :warehouseId")
  List<Stock> findByWarehouseId(@Param("warehouseId") Long warehouseId);

  @Query("SELECT s FROM Stock s WHERE s.product.id = :productId")
  List<Stock> findByProductId(@Param("productId") Long productId);

  boolean existsByProductAndWarehouse(Product product, Warehouse warehouse);

    Optional<Stock> findByProductIdAndWarehouseId(Long productId, Long warehouseId);
}
