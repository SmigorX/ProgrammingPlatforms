package com.marketviz.service;

import com.marketviz.dto.auth.LoginRequest;
import com.marketviz.dto.auth.RegisterRequest;
import com.marketviz.dto.auth.TokenResponse;
import com.marketviz.exception.ApiException;
import com.marketviz.model.User;
import com.marketviz.repository.UserRepository;
import com.marketviz.security.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles user registration and authentication.
 *
 * <p>Credential verification is delegated to Spring Security's
 * {@link AuthenticationManager}; JWT issuance is handled by
 * {@link JwtTokenProvider}. The service itself never stores or compares
 * plaintext passwords.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authManager,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authManager     = authManager;
        this.tokenProvider   = tokenProvider;
    }

    /**
     * Creates a new account and immediately returns a token for the new user.
     *
     * @throws ApiException HTTP 409 if the username or e-mail is already taken
     */
    @Transactional
    public TokenResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ApiException(HttpStatus.CONFLICT, "Username already taken");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already registered");
        }
        var user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        userRepository.save(user);

        return issueToken(request.username(), request.password());
    }

    /**
     * Authenticates an existing user and returns a JWT.
     *
     * @throws org.springframework.security.core.AuthenticationException if credentials are invalid
     */
    public TokenResponse login(LoginRequest request) {
        return issueToken(request.username(), request.password());
    }

    private TokenResponse issueToken(String username, String password) {
        var authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        var token = tokenProvider.generateToken(authentication);
        var user  = userRepository.findByUsername(username).orElseThrow();
        return new TokenResponse(token, user.getUsername(), user.getRole().name());
    }
}
