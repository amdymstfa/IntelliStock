package com.logistics.intellistock.mapper;

import com.logistics.intellistock.dto.request.RegisterRequest;
import com.logistics.intellistock.dto.response.AuthResponse;
import com.logistics.intellistock.entity.User;
import com.logistics.intellistock.entity.enums.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = {Role.class})
public interface AuthMapper {

    @Mapping(target = "token", source = "token")
    @Mapping(target = "type", constant = "Bearer")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "role", source = "user.role")
    @Mapping(target = "warehouseId", source = "user.warehouse.id")
    @Mapping(target = "warehouseName", source = "user.warehouse.name")
    AuthResponse toAuthResponse(User user, String token);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "warehouse", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(RegisterRequest request);
}