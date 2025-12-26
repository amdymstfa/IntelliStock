package com.logistics.intellistock.service;

import com.logistics.intellistock.core.exception.DuplicateResourceException;
import com.logistics.intellistock.core.exception.UnauthorizedException;
import com.logistics.intellistock.dto.request.LoginRequest;
import com.logistics.intellistock.dto.request.RegisterRequest;
import com.logistics.intellistock.dto.response.AuthResponse;
import com.logistics.intellistock.entity.User;
import com.logistics.intellistock.entity.enums.Role;
import com.logistics.intellistock.mapper.AuthMapper;
import com.logistics.intellistock.repository.UserRepository;
import com.logistics.intellistock.security.JwtUtil;
import com.logistics.intellistock.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {
    @Mock
    private UserRepository repo;
    @Mock private AuthenticationManager authManager;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthMapper mapper;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("ser-jyoxin")
                .password("encoded-pwd")
                .role(Role.ADMIN)
                .isActive(true)
                .build();

        loginRequest = new LoginRequest("ser-jyoxin", "Password123@");

        registerRequest = new RegisterRequest(
                "Password123@",
                "ser-jyoxin",
                "test@mail.com",
                Role.ADMIN,
                1L
        );

        authResponse = new AuthResponse(
                "token-123",
                "Bearer",
                "ser-jyoxin",
                "test@mail.com",
                Role.ADMIN,
                1L,
                "Main Warehouse"
        );
    }

    // --- LOGIN TESTS ---

    @Test
    @DisplayName("Should login successfully")
    void shouldLoginSuccessfully() {
        // Arrange
        Authentication auth = mock(Authentication.class);
        when(authManager.authenticate(any())).thenReturn(auth);
        when(jwtUtil.generateJwt(auth)).thenReturn("fake-jwt-token");
        when(repo.findByUsername(loginRequest.getUsername())).thenReturn(Optional.of(testUser));
        when(mapper.toAuthResponse(testUser, "fake-jwt-token")).thenReturn(authResponse);

        // Act
        AuthResponse result = authService.login(loginRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("token-123");
        verify(authManager).authenticate(any());
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when credentials are invalid")
    void shouldThrowExceptionWhenBadCredentials() {
        // Arrange
        when(authManager.authenticate(any())).thenThrow(new BadCredentialsException("Invalid"));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UnauthorizedException.class);
    }

    // --- REGISTER TESTS ---

    @Test
    @DisplayName("Should register successfully")
    void shouldRegisterSuccessfully() {
        // Arrange
        when(repo.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(mapper.toEntity(registerRequest)).thenReturn(testUser);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encoded-pwd");
        when(repo.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateJwt(any())).thenReturn("token-123");
        when(mapper.toAuthResponse(any(), anyString())).thenReturn(authResponse);

        // Act
        AuthResponse result = authService.register(registerRequest);

        // Assert
        assertThat(result).isNotNull();
        verify(repo).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when username exists")
    void shouldThrowExceptionWhenUserExists() {
        // Arrange
        when(repo.existsByUsername(registerRequest.getUsername())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateResourceException.class);

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when authenticated user not found in DB")
    void shouldThrowExceptionWhenUserNotInDbAfterAuth() {
        // Arrange
        when(authManager.authenticate(any())).thenReturn(mock(Authentication.class));
        when(repo.findByUsername(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UnauthorizedException.class);
    }
}
