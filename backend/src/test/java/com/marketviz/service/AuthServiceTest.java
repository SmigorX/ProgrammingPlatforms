package com.marketviz.service;

import com.marketviz.dto.auth.RegisterRequest;
import com.marketviz.exception.ApiException;
import com.marketviz.model.User;
import com.marketviz.repository.UserRepository;
import com.marketviz.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthenticationManager authManager;
    @Mock JwtTokenProvider tokenProvider;

    @InjectMocks AuthService authService;

    @Test
    void register_throwsConflict_whenUsernameAlreadyTaken() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("alice", "alice@example.com", "password123")
        )).isInstanceOf(ApiException.class)
          .hasMessageContaining("Username already taken");
    }

    @Test
    void register_throwsConflict_whenEmailAlreadyRegistered() {
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("alice", "alice@example.com", "password123")
        )).isInstanceOf(ApiException.class)
          .hasMessageContaining("Email already registered");
    }

    @Test
    void register_savesHashedPasswordAndReturnsToken() {
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(authManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken("alice", null)
        );
        when(tokenProvider.generateToken(any())).thenReturn("jwt.token.here");

        var savedUser = new User();
        savedUser.setUsername("alice");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(savedUser));

        var response = authService.register(
                new RegisterRequest("alice", "alice@example.com", "password123")
        );

        assertThat(response.accessToken()).isEqualTo("jwt.token.here");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(argThat(u -> "hashed".equals(u.getPasswordHash())));
    }
}
