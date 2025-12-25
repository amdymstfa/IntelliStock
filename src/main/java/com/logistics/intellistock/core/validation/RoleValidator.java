package com.logistics.intellistock.core.validation;

import com.logistics.intellistock.enums.Role;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RoleValidator implements ConstraintValidator<ValidRole, Role> {

  @Override
  public void initialize(ValidRole constraintAnnotation) {
    // No initialization needed
  }

  @Override
  public boolean isValid(Role role, ConstraintValidatorContext context) {
    if (role == null) {
      return false;
    }

    // Check if the role is one of the valid enum values
    try {
      return role == Role.ADMIN || role == Role.MANAGER;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
