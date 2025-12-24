package com.logistics.intellistock.mapper;

import com.logistics.intellistock.dto.response.UserResponse;
import com.logistics.intellistock.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

  @Mapping(target = "warehouseId", source = "warehouse.id")
  @Mapping(target = "warehouseName", source = "warehouse.name")
  UserResponse toResponse(User user);

  List<UserResponse> toResponseList(List<User> users);
}
