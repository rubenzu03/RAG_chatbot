package com.rubenzu03.rag_chatbot.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthenticationManagerServiceTest {

    private AuthenticationManager authenticationManager;
    private AuthenticationManagerService service;

    @BeforeEach
    void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        service = new AuthenticationManagerService(authenticationManager);
    }

    @Test
    void authenticate_delegatesToAuthenticationManager() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        Authentication result = service.authenticate("user", "pass");

        assertThat(result).isSameAs(authentication);
    }
}

