package com.logistics.intellistock.dto.response;

import com.logistics.intellistock.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private String username;
    private String email;
    private Role role;

    private Long warehouseId;
    private String warehouseName;
}