package com.logistics.intellistock.service;

import com.logistics.intellistock.dto.request.CreateWarehouseRequest;
import com.logistics.intellistock.dto.request.UpdateWarehouseRequest;
import com.logistics.intellistock.dto.response.WarehouseResponse;
import com.logistics.intellistock.entity.Warehouse;
import com.logistics.intellistock.core.exception.DuplicateResourceException;
import com.logistics.intellistock.core.exception.ResourceNotFoundException;
import com.logistics.intellistock.mapper.WarehouseMapper;
import com.logistics.intellistock.repository.WarehouseRepository;
import com.logistics.intellistock.service.impl.WarehouseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Warehouse Service Tests")
class WarehouseServiceTest {

  @Mock
  private WarehouseRepository warehouseRepository;

  @Mock
  private WarehouseMapper warehouseMapper;

  @InjectMocks
  private WarehouseServiceImpl warehouseService;

  private Warehouse testWarehouse;
  private CreateWarehouseRequest createRequest;
  private UpdateWarehouseRequest updateRequest;
  private WarehouseResponse warehouseResponse;

  @BeforeEach
  void setUp() {
    testWarehouse = Warehouse.builder()
      .id(1L)
      .name("New York Central Warehouse")
      .city("New York")
      .address("1250 Broadway Avenue")
      .country("USA")
      .postalCode("10001")
      .phone("+1-212-555-0101")
      .email("ny.warehouse@intellistock.com")
      .capacity(50000)
      .isActive(true)
      .build();

    createRequest = new CreateWarehouseRequest(
      "New York Central Warehouse",
      "New York",
      "1250 Broadway Avenue",
      "USA",
      "10001",
      "+1-212-555-0101",
      "ny.warehouse@intellistock.com",
      50000
    );

    updateRequest = new UpdateWarehouseRequest();
    updateRequest.setCapacity(60000);
    updateRequest.setPhone("+1-212-555-0102");

    warehouseResponse = WarehouseResponse.builder()
      .id(1L)
      .name("New York Central Warehouse")
      .city("New York")
      .capacity(50000)
      .isActive(true)
      .build();
  }

  @Test
  @DisplayName("Should create warehouse successfully")
  void shouldCreateWarehouseSuccessfully() {
    // Given
    when(warehouseRepository.existsByName(createRequest.getName())).thenReturn(false);
    when(warehouseRepository.save(any(Warehouse.class))).thenReturn(testWarehouse);
    when(warehouseMapper.toResponse(any(Warehouse.class))).thenReturn(warehouseResponse);

    // When
    WarehouseResponse result = warehouseService.createWarehouse(createRequest);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("New York Central Warehouse");
    assertThat(result.getCity()).isEqualTo("New York");

    verify(warehouseRepository).existsByName(createRequest.getName());
    verify(warehouseRepository).save(any(Warehouse.class));
  }

  @Test
  @DisplayName("Should throw exception when creating warehouse with duplicate name")
  void shouldThrowExceptionWhenCreatingWarehouseWithDuplicateName() {
    // Given
    when(warehouseRepository.existsByName(createRequest.getName())).thenReturn(true);

    // When & Then
    assertThatThrownBy(() -> warehouseService.createWarehouse(createRequest))
      .isInstanceOf(DuplicateResourceException.class)
      .hasMessageContaining("name");

    verify(warehouseRepository).existsByName(createRequest.getName());
    verify(warehouseRepository, never()).save(any(Warehouse.class));
  }

  @Test
  @DisplayName("Should get warehouse by ID successfully")
  void shouldGetWarehouseByIdSuccessfully() {
    // Given
    when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));
    when(warehouseMapper.toResponse(testWarehouse)).thenReturn(warehouseResponse);

    // When
    WarehouseResponse result = warehouseService.getWarehouseById(1L);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getName()).isEqualTo("New York Central Warehouse");
    verify(warehouseRepository).findById(1L);
  }

  @Test
  @DisplayName("Should throw exception when warehouse not found")
  void shouldThrowExceptionWhenWarehouseNotFound() {
    // Given
    when(warehouseRepository.findById(999L)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> warehouseService.getWarehouseById(999L))
      .isInstanceOf(ResourceNotFoundException.class)
      .hasMessageContaining("Warehouse");

    verify(warehouseRepository).findById(999L);
  }

  @Test
  @DisplayName("Should get all active warehouses")
  void shouldGetAllActiveWarehouses() {
    // Given
    List<Warehouse> warehouses = Arrays.asList(testWarehouse, testWarehouse);
    when(warehouseRepository.findByIsActive(true)).thenReturn(warehouses);
    when(warehouseMapper.toResponseList(warehouses))
      .thenReturn(Arrays.asList(warehouseResponse, warehouseResponse));

    // When
    List<WarehouseResponse> result = warehouseService.getAllWarehouses();

    // Then
    assertThat(result).hasSize(2);
    verify(warehouseRepository).findByIsActive(true);
  }

  @Test
  @DisplayName("Should update warehouse successfully")
  void shouldUpdateWarehouseSuccessfully() {
    // Given
    when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));
    when(warehouseRepository.save(any(Warehouse.class))).thenReturn(testWarehouse);
    when(warehouseMapper.toResponse(any(Warehouse.class))).thenReturn(warehouseResponse);

    // When
    WarehouseResponse result = warehouseService.updateWarehouse(1L, updateRequest);

    // Then
    assertThat(result).isNotNull();
    verify(warehouseRepository).findById(1L);
    verify(warehouseRepository).save(any(Warehouse.class));
  }

  @Test
  @DisplayName("Should update warehouse name and check for duplicates")
  void shouldCheckDuplicateWhenUpdatingWarehouseName() {
    // Given
    UpdateWarehouseRequest requestWithNewName = new UpdateWarehouseRequest();
    requestWithNewName.setName("Los Angeles Warehouse");

    when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));
    when(warehouseRepository.existsByName("Los Angeles Warehouse")).thenReturn(false);
    when(warehouseRepository.save(any(Warehouse.class))).thenReturn(testWarehouse);
    when(warehouseMapper.toResponse(any(Warehouse.class))).thenReturn(warehouseResponse);

    // When
    WarehouseResponse result = warehouseService.updateWarehouse(1L, requestWithNewName);

    // Then
    assertThat(result).isNotNull();
    verify(warehouseRepository).existsByName("Los Angeles Warehouse");
  }

  @Test
  @DisplayName("Should throw exception when updating to duplicate name")
  void shouldThrowExceptionWhenUpdatingToDuplicateName() {
    // Given
    UpdateWarehouseRequest requestWithDuplicateName = new UpdateWarehouseRequest();
    requestWithDuplicateName.setName("Existing Warehouse");

    when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));
    when(warehouseRepository.existsByName("Existing Warehouse")).thenReturn(true);

    // When & Then
    assertThatThrownBy(() -> warehouseService.updateWarehouse(1L, requestWithDuplicateName))
      .isInstanceOf(DuplicateResourceException.class)
      .hasMessageContaining("name");

    verify(warehouseRepository, never()).save(any(Warehouse.class));
  }

  @Test
  @DisplayName("Should delete warehouse (soft delete)")
  void shouldDeleteWarehouseSoftly() {
    // Given
    when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));
    when(warehouseRepository.save(any(Warehouse.class))).thenReturn(testWarehouse);

    // When
    warehouseService.deleteWarehouse(1L);

    // Then
    verify(warehouseRepository).findById(1L);
    verify(warehouseRepository).save(argThat(warehouse ->
      !warehouse.getIsActive()
    ));
  }

  @Test
  @DisplayName("Should return empty list when no warehouses found")
  void shouldReturnEmptyListWhenNoWarehousesFound() {
    // Given
    when(warehouseRepository.findByIsActive(true)).thenReturn(Arrays.asList());
    when(warehouseMapper.toResponseList(anyList())).thenReturn(Arrays.asList());

    // When
    List<WarehouseResponse> result = warehouseService.getAllWarehouses();

    // Then
    assertThat(result).isEmpty();
    verify(warehouseRepository).findByIsActive(true);
  }

  @Test
  @DisplayName("Should update only provided fields")
  void shouldUpdateOnlyProvidedFields() {
    // Given
    UpdateWarehouseRequest partialUpdate = new UpdateWarehouseRequest();
    partialUpdate.setCapacity(70000);
    // Other fields are null

    when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));
    when(warehouseRepository.save(any(Warehouse.class))).thenReturn(testWarehouse);
    when(warehouseMapper.toResponse(any(Warehouse.class))).thenReturn(warehouseResponse);

    // When
    WarehouseResponse result = warehouseService.updateWarehouse(1L, partialUpdate);

    // Then
    assertThat(result).isNotNull();
    verify(warehouseRepository).save(argThat(warehouse ->
      warehouse.getName().equals("New York Central Warehouse") // Name unchanged
    ));
  }
}
