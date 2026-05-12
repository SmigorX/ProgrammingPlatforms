package com.marketviz.dto.auth;

/** JWT access token returned after successful authentication or registration. */
public record TokenResponse(String accessToken, String tokenType, String username, String role) {

    public TokenResponse(String accessToken, String username, String role) {
        this(accessToken, "Bearer", username, role);
    }
}
