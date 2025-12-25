package com.logistics.intellistock.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
public class LoginRequest {
    @NotBlank(message = "Login required")
    private String username;

    @NotBlank(message = "Password required")
    private String password;
}