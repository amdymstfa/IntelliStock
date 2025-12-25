package com.logistics.intellistock.service.impl;

import com.logistics.intellistock.dto.response.UserResponse;
import com.logistics.intellistock.entity.User;
import com.logistics.intellistock.entity.Warehouse;
import com.logistics.intellistock.core.exception.ResourceNotFoundException;
import com.logistics.intellistock.mapper.UserMapper;
import com.logistics.intellistock.repository.UserRepository;
import com.logistics.intellistock.repository.WarehouseRepository;
import com.logistics.intellistock.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final WarehouseRepository warehouseRepository;
  private final UserMapper userMapper;

  @Override
  @Transactional(readOnly = true)
  public List<UserResponse> getAllUsers() {
    List<User> users = userRepository.findAll();
    return userMapper.toResponseList(users);
  }

  @Override
  @Transactional(readOnly = true)
  public UserResponse getUserById(Long id) {
    User user = userRepository.findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    return userMapper.toResponse(user);
  }

  @Override
  @Transactional
  public UserResponse updateUser(Long id, User updatedUser) {
    log.info("Updating user with ID: {}", id);

    User user = userRepository.findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

    if (updatedUser.getUsername() != null) {
      user.setUsername(updatedUser.getUsername());
    }
    if (updatedUser.getEmail() != null) {
      user.setEmail(updatedUser.getEmail());
    }
    if (updatedUser.getIsActive() != null) {
      user.setIsActive(updatedUser.getIsActive());
    }

    User saved = userRepository.save(user);
    log.info("User updated successfully: {}", saved.getUsername());

    return userMapper.toResponse(saved);
  }

  @Override
  @Transactional
  public void deleteUser(Long id) {
    log.info("Deleting user with ID: {}", id);

    User user = userRepository.findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

    user.setIsActive(false);
    userRepository.save(user);

    log.info("User deactivated successfully: {}", user.getUsername());
  }

  @Override
  @Transactional
  public void assignWarehouse(Long userId, Long warehouseId) {
    log.info("Assigning warehouse {} to user {}", warehouseId, userId);

    User user = userRepository.findById(userId)
      .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

    Warehouse warehouse = warehouseRepository.findById(warehouseId)
      .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", warehouseId));

    user.setWarehouse(warehouse);
    userRepository.save(user);

    log.info("Warehouse assigned successfully");
  }

  @Override
  @Transactional(readOnly = true)
  public List<UserResponse> getUsersByWarehouse(Long warehouseId) {
    Warehouse warehouse = warehouseRepository.findById(warehouseId)
      .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", warehouseId));

    List<User> users = userRepository.findByWarehouse(warehouse);
    return userMapper.toResponseList(users);
  }
}
