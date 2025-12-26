package com.logistics.intellistock.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RoleValidator.class)
@Documented
public @interface ValidRole {

  String message() default "Invalid role";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
