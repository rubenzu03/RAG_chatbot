package com.rubenzu03.rag_chatbot.application.service;

import com.rubenzu03.rag_chatbot.application.ports.output.UserRepositoryPort;
import com.rubenzu03.rag_chatbot.domain.model.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private UserRepositoryPort userRepositoryPort;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManagerService authenticationManagerService;
    private JwtUtilsService jwtUtilsService;
    private UserService service;

    @BeforeEach
    void setUp() {
        userRepositoryPort = mock(UserRepositoryPort.class);
        passwordEncoder = mock(PasswordEncoder.class);
        authenticationManagerService = mock(AuthenticationManagerService.class);
        jwtUtilsService = mock(JwtUtilsService.class);
        service = new UserService(userRepositoryPort, passwordEncoder, authenticationManagerService, jwtUtilsService);
    }

    @Test
    void login_updatesLastLoginAndReturnsToken() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@example.com");
        when(authenticationManagerService.authenticate("user@example.com", "pw")).thenReturn(auth);
        UserDTO user = new UserDTO();
        user.setEmail("user@example.com");
        when(userRepositoryPort.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(jwtUtilsService.generateToken("user@example.com")).thenReturn("token");

        String token = service.login("user@example.com", "pw");

        assertThat(token).isEqualTo("token");
        verify(userRepositoryPort).save(any(UserDTO.class));
    }

    @Test
    void registerUser_throwsWhenExists() {
        UserDTO user = new UserDTO();
        user.setEmail("user@example.com");
        when(userRepositoryPort.existsByEmail("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.registerUser(user))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User already exists");
    }

    @Test
    void registerUser_encodesPasswordAndSaves() {
        UserDTO user = new UserDTO();
        user.setEmail("user@example.com");
        user.setPassword("plain");
        when(userRepositoryPort.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plain")).thenReturn("encoded");
        when(userRepositoryPort.save(any(UserDTO.class))).thenReturn(user);

        UserDTO saved = service.registerUser(user);

        assertThat(saved).isNotNull();
        assertThat(user.getPassword()).isEqualTo("encoded");
    }

    @Test
    void getUserProfile_missing_throws() {
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUserProfile(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }
}

