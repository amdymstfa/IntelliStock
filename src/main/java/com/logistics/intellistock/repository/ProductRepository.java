
package com.logistics.intellistock.repository;

import com.logistics.intellistock.entity.Product;
import com.logistics.intellistock.entity.Warehouse;
import com.logistics.intellistock.enums.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

  Optional<Product> findBySku(String sku);

  List<Product> findByCategory(Category category);

  List<Product> findByIsActive(Boolean isActive);

  boolean existsBySku(String sku);

  @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
  List<Product> searchByName(@Param("keyword") String keyword);

  @Query("SELECT p FROM Product p WHERE p.category = :category AND p.isActive = true")
  List<Product> findActiveByCategoryy(@Param("category") Category category);
}
