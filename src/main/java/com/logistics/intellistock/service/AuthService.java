package com.logistics.intellistock.service;

import com.logistics.intellistock.dto.request.LoginRequest;
import com.logistics.intellistock.dto.request.RegisterRequest;
import com.logistics.intellistock.dto.response.AuthResponse;
import com.logistics.intellistock.dto.response.UserResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
}
