package com.logistics.intellistock.service.impl;

import com.logistics.intellistock.dto.request.CreateWarehouseRequest;
import com.logistics.intellistock.dto.request.UpdateWarehouseRequest;
import com.logistics.intellistock.dto.response.WarehouseResponse;
import com.logistics.intellistock.entity.Warehouse;
import com.logistics.intellistock.core.exception.DuplicateResourceException;
import com.logistics.intellistock.core.exception.ResourceNotFoundException;
import com.logistics.intellistock.mapper.WarehouseMapper;
import com.logistics.intellistock.repository.WarehouseRepository;
import com.logistics.intellistock.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

  private final WarehouseRepository warehouseRepository;
  private final WarehouseMapper warehouseMapper;

  @Override
  @Transactional
  public WarehouseResponse createWarehouse(CreateWarehouseRequest request) {
    log.info("Creating new warehouse: {}", request.getName());

    // Check if warehouse name already exists
    if (warehouseRepository.existsByName(request.getName())) {
      throw new DuplicateResourceException("Warehouse", "name", request.getName());
    }

    Warehouse warehouse = Warehouse.builder()
      .name(request.getName())
      .city(request.getCity())
      .address(request.getAddress())
      .country(request.getCountry())
      .postalCode(request.getPostalCode())
      .phone(request.getPhone())
      .email(request.getEmail())
      .capacity(request.getCapacity())
      .isActive(true)
      .build();

    Warehouse savedWarehouse = warehouseRepository.save(warehouse);
    log.info("Warehouse created successfully: {}", savedWarehouse.getName());

    return warehouseMapper.toResponse(savedWarehouse);
  }

  @Override
  @Transactional(readOnly = true)
  public WarehouseResponse getWarehouseById(Long id) {
    Warehouse warehouse = warehouseRepository.findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", id));
    return warehouseMapper.toResponse(warehouse);
  }

  @Override
  @Transactional(readOnly = true)
  public List<WarehouseResponse> getAllWarehouses() {
    List<Warehouse> warehouses = warehouseRepository.findByIsActive(true);
    return warehouseMapper.toResponseList(warehouses);
  }

  @Override
  @Transactional
  public WarehouseResponse updateWarehouse(Long id, UpdateWarehouseRequest request) {
    log.info("Updating warehouse with ID: {}", id);

    Warehouse warehouse = warehouseRepository.findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", id));

    // Check for duplicate name if name is being updated
    if (request.getName() != null && !request.getName().equals(warehouse.getName())) {
      if (warehouseRepository.existsByName(request.getName())) {
        throw new DuplicateResourceException("Warehouse", "name", request.getName());
      }
      warehouse.setName(request.getName());
    }

    if (request.getCity() != null) {
      warehouse.setCity(request.getCity());
    }
    if (request.getAddress() != null) {
      warehouse.setAddress(request.getAddress());
    }
    if (request.getCountry() != null) {
      warehouse.setCountry(request.getCountry());
    }
    if (request.getPostalCode() != null) {
      warehouse.setPostalCode(request.getPostalCode());
    }
    if (request.getPhone() != null) {
      warehouse.setPhone(request.getPhone());
    }
    if (request.getEmail() != null) {
      warehouse.setEmail(request.getEmail());
    }
    if (request.getCapacity() != null) {
      warehouse.setCapacity(request.getCapacity());
    }
    if (request.getIsActive() != null) {
      warehouse.setIsActive(request.getIsActive());
    }

    Warehouse updatedWarehouse = warehouseRepository.save(warehouse);
    log.info("Warehouse updated successfully: {}", updatedWarehouse.getName());

    return warehouseMapper.toResponse(updatedWarehouse);
  }

  @Override
  @Transactional
  public void deleteWarehouse(Long id) {
    log.info("Deleting warehouse with ID: {}", id);

    Warehouse warehouse = warehouseRepository.findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", id));

    // Soft delete
    warehouse.setIsActive(false);
    warehouseRepository.save(warehouse);

    log.info("Warehouse deleted successfully: {}", warehouse.getName());
  }
}
