package com.logistics.intellistock.repository;

import com.logistics.intellistock.entity.User;
import com.logistics.intellistock.entity.Warehouse;
import com.logistics.intellistock.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByUsername(String username);

  Optional<User> findByUsername(String login);

  Optional<User> findByEmail(String email);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);

  List<User> findByRole(Role role);

  List<User> findByWarehouse(Warehouse warehouse);

  List<User> findByIsActive(Boolean isActive);

  Optional<User> findByUsernameAndIsActive(String username, Boolean isActive);
}
