package com.logistics.intellistock.repository;

import com.logistics.intellistock.entity.Product;
import com.logistics.intellistock.entity.SalesHistory;
import com.logistics.intellistock.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalesHistoryRepository extends JpaRepository<SalesHistory, Long> {

  List<SalesHistory> findByProductAndWarehouse(Product product, Warehouse warehouse);

  List<SalesHistory> findByWarehouse(Warehouse warehouse);

  List<SalesHistory> findByProduct(Product product);

  @Query("SELECT s FROM SalesHistory s WHERE s.product = :product AND s.warehouse = :warehouse " +
    "AND s.saleDate BETWEEN :startDate AND :endDate ORDER BY s.saleDate DESC")
  List<SalesHistory> findByProductAndWarehouseBetweenDates(
    @Param("product") Product product,
    @Param("warehouse") Warehouse warehouse,
    @Param("startDate") LocalDate startDate,
    @Param("endDate") LocalDate endDate
  );

  @Query("SELECT s FROM SalesHistory s WHERE s.warehouse = :warehouse " +
    "AND s.saleDate BETWEEN :startDate AND :endDate ORDER BY s.saleDate DESC")
  List<SalesHistory> findByWarehouseBetweenDates(
    @Param("warehouse") Warehouse warehouse,
    @Param("startDate") LocalDate startDate,
    @Param("endDate") LocalDate endDate
  );

  @Query("SELECT SUM(s.quantitySold) FROM SalesHistory s WHERE s.product = :product " +
    "AND s.warehouse = :warehouse AND s.saleDate >= :startDate")
  Long sumQuantitySoldSinceDate(
    @Param("product") Product product,
    @Param("warehouse") Warehouse warehouse,
    @Param("startDate") LocalDate startDate
  );

  @Query("SELECT s FROM SalesHistory s WHERE s.product.id = :productId AND s.warehouse.id = :warehouseId " +
    "ORDER BY s.saleDate DESC")
  List<SalesHistory> findByProductIdAndWarehouseId(
    @Param("productId") Long productId,
    @Param("warehouseId") Long warehouseId
  );

    List<SalesHistory> findByProductIdAndWarehouseIdAndYearBetween(Long productId, Long warehouseId, Integer startYear, Integer endYear);
    List<SalesHistory> findByProductIdAndWarehouseIdAndSaleDateBetween(Long productId, Long warehouseId, LocalDate startDate, LocalDate endDate);
}
