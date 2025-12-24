package com.logistics.intellistock.service;

import com.logistics.intellistock.dto.response.UserResponse;
import com.logistics.intellistock.entity.User;

import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers();
    UserResponse getUserById(Long id);
    UserResponse updateUser(Long id, User user);
    void deleteUser(Long id);
    void assignWarehouse(Long userId, Long warehouseId);
    List<UserResponse> getUsersByWarehouse(Long warehouseId);
}
