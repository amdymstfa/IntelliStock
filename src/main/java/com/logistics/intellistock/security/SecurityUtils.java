package com.logistics.intellistock.security;

import com.logistics.intellistock.entity.User;
import com.logistics.intellistock.entity.enums.Role;
import com.logistics.intellistock.core.exception.UnauthorizedException;
import com.logistics.intellistock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

  private final UserRepository userRepository;

  public UserPrincipal getCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null || !auth.isAuthenticated()) {
      throw new UnauthorizedException("No authenticated user found");
    }

    UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();


    return userPrincipal;
  }

  public boolean isAdmin() {
    UserPrincipal principal = getCurrentUser();
    return principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN"));
  }

  public boolean isManager() {
    UserPrincipal principal = getCurrentUser();
    return principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("MANAGER"));
  }

  public boolean hasAccessToWarehouse(Long warehouseId) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null || !auth.isAuthenticated()) {
      return false;
    }

    UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();

    boolean isAdmin = userPrincipal.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ADMIN"));

    if (isAdmin) return true;

    return Objects.equals(userPrincipal.getWarehouseId(), warehouseId);
  }

  public void validateWarehouseAccess(Long warehouseId) {
    if (!hasAccessToWarehouse(warehouseId)) {
      throw new UnauthorizedException(
        "You don't have permission to access this warehouse"
      );
    }
  }
}
