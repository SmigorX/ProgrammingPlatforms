package com.marketviz.dto.auth;

import jakarta.validation.constraints.NotBlank;

/** Credentials submitted to {@code POST /api/auth/login}. */
public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password
) {}
