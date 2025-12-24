package com.logistics.intellistock.repository;

import com.logistics.intellistock.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

  Optional<Warehouse> findByName(String name);

  List<Warehouse> findByCity(String city);

  List<Warehouse> findByIsActive(Boolean isActive);

  boolean existsByName(String name);
}

