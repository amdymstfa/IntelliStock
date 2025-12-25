package com.logistics.intellistock.dto.request;

import com.logistics.intellistock.entity.enums.Role;
import com.logistics.intellistock.entity.enums.Role;
import com.logistics.intellistock.core.validation.ValidEmail;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class RegisterRequest {
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$",
            message = "Password must contain at least one uppercase, one lowercase, one number and one special character"
    )
    private String password;

    @NotBlank(message = "Username is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @ValidEmail
    private String email;

    @NotNull(message = "Role is required")
    private Role role;

    private Long warehouseId;
}