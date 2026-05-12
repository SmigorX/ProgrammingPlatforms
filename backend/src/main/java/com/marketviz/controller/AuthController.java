package com.marketviz.controller;

import com.marketviz.dto.auth.LoginRequest;
import com.marketviz.dto.auth.RegisterRequest;
import com.marketviz.dto.auth.TokenResponse;
import com.marketviz.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Handles user registration and login, returning JWT access tokens.
 * These endpoints are publicly accessible (no Bearer token required).
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "User registration and authentication")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new account and receive a JWT")
    public TokenResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate with existing credentials and receive a JWT")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
