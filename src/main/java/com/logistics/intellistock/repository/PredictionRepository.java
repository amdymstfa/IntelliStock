package com.logistics.intellistock.repository;

import com.logistics.intellistock.entity.Prediction;
import com.logistics.intellistock.entity.Product;
import com.logistics.intellistock.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Long> {

  List<Prediction> findByProductAndWarehouse(Product product, Warehouse warehouse);

  List<Prediction> findByWarehouse(Warehouse warehouse);

  List<Prediction> findByProduct(Product product);

  @Query("SELECT p FROM Prediction p WHERE p.product = :product AND p.warehouse = :warehouse " +
    "ORDER BY p.predictionDate DESC")
  List<Prediction> findLatestByProductAndWarehouse(
    @Param("product") Product product,
    @Param("warehouse") Warehouse warehouse
  );

  @Query("SELECT p FROM Prediction p WHERE p.warehouse = :warehouse " +
    "AND p.predictionDate = (SELECT MAX(p2.predictionDate) FROM Prediction p2 " +
    "WHERE p2.product = p.product AND p2.warehouse = p.warehouse)")
  List<Prediction> findLatestPredictionsByWarehouse(@Param("warehouse") Warehouse warehouse);

  @Query("SELECT p FROM Prediction p WHERE p.product.id = :productId " +
    "AND p.warehouse.id = :warehouseId AND p.predictionDate >= :date")
  List<Prediction> findByProductIdAndWarehouseIdAfterDate(
    @Param("productId") Long productId,
    @Param("warehouseId") Long warehouseId,
    @Param("date") LocalDate date
  );

  Optional<Prediction> findTopByProductAndWarehouseOrderByPredictionDateDesc(
    Product product,
    Warehouse warehouse
  );
}
