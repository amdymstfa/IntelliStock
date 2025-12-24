package com.logistics.intellistock.service;

import com.logistics.intellistock.dto.request.LoginRequest;
import com.logistics.intellistock.dto.request.RegisterRequest;
import com.logistics.intellistock.dto.response.LoginResponse;
import com.logistics.intellistock.dto.response.UserResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    UserResponse register(RegisterRequest request);
}
