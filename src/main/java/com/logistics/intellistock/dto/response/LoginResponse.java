package com.logistics.intellistock.dto.response;

import com.logistics.intellistock.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String token;
    private String type = "Bearer";
    private Long userId;
    private String login;
    private String email;
    private Role role;
    private Long warehouseId;
    private String warehouseName;
}
