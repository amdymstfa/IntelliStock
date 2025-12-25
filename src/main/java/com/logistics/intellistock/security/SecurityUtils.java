package com.logistics.intellistock.security;

import com.logistics.intellistock.entity.User;
import com.logistics.intellistock.enums.Role;
import com.logistics.intellistock.exception.UnauthorizedException;
import com.logistics.intellistock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

  private final UserRepository userRepository;

  public User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      throw new UnauthorizedException("No authenticated user found");
    }

    String username = authentication.getName();
    return userRepository.findByLogin(username)
      .orElseThrow(() -> new UnauthorizedException("User not found"));
  }

  public boolean isAdmin() {
    User user = getCurrentUser();
    return user.getRole() == Role.ADMIN;
  }

  public boolean isManager() {
    User user = getCurrentUser();
    return user.getRole() == Role.MANAGER;
  }

  public boolean hasAccessToWarehouse(Long warehouseId) {
    User user = getCurrentUser();

    // Admin has access to all warehouses
    if (user.getRole() == Role.ADMIN) {
      return true;
    }

    // Manager can only access their assigned warehouse
    if (user.getRole() == Role.MANAGER) {
      return user.getWarehouse() != null &&
        user.getWarehouse().getId().equals(warehouseId);
    }

    return false;
  }

  public void validateWarehouseAccess(Long warehouseId) {
    if (!hasAccessToWarehouse(warehouseId)) {
      throw new UnauthorizedException(
        "You don't have permission to access this warehouse"
      );
    }
  }
}
