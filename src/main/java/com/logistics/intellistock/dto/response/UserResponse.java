package com.logistics.intellistock.dto.response;

import com.logistics.intellistock.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String login;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private Boolean isActive;
    private Long warehouseId;
    private String warehouseName;
    private LocalDateTime createdAt;
}
