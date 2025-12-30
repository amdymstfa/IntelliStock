package com.logistics.intellistock.service.impl;

import com.logistics.intellistock.core.exception.DuplicateResourceException;
import com.logistics.intellistock.core.exception.UnauthorizedException;
import com.logistics.intellistock.dto.request.LoginRequest;
import com.logistics.intellistock.dto.request.RegisterRequest;
import com.logistics.intellistock.dto.response.AuthResponse;
import com.logistics.intellistock.entity.User;
import com.logistics.intellistock.mapper.AuthMapper;
import com.logistics.intellistock.repository.UserRepository;
import com.logistics.intellistock.security.JwtUtil;
import com.logistics.intellistock.security.UserPrincipal;
import com.logistics.intellistock.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepository repo;
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final AuthMapper mapper;
    private final PasswordEncoder passwordEncoder;


    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            String token = jwtUtil.generateJwt(authentication);

            User user = this.getUserByUsername(request.getUsername());

            log.info("User '{}' logged in successfully", request.getUsername());

            return mapper.toAuthResponse(user, token);
        } catch (Exception e) {
            log.error("Invalid credentials for user: {}", request.getUsername());
            throw new UnauthorizedException("Invalid credentials for user: " + request.getUsername());
        }
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (repo.existsByUsername(request.getUsername())) {
            log.error("Username '{}' is already taken", request.getUsername());
            throw new DuplicateResourceException("Username is already taken: " + request.getUsername());
        }

        User newUser = mapper.toEntity(request);
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setIsActive(true);


        User savedUser = repo.save(newUser);

        UserPrincipal userPrincipal = UserPrincipal.create(savedUser);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal,
                null,
                userPrincipal.getAuthorities()
        );

        String token = jwtUtil.generateJwt(authentication);

        log.info("User '{}' registered and logged in successfully", request.getUsername());

        return mapper.toAuthResponse(savedUser, token);
    }

    private User getUserByUsername(String username) throws Exception {
        return repo.findByUsername(username)
                .orElseThrow(() -> new Exception("User not found with username: " + username));
    }
}