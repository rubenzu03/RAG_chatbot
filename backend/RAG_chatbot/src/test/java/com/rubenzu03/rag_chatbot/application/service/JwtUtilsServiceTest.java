package com.rubenzu03.rag_chatbot.application.service;

import com.rubenzu03.rag_chatbot.auth.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtUtilsServiceTest {

    private JwtUtil jwtUtil;
    private JwtUtilsService service;

    @BeforeEach
    void setUp() {
        jwtUtil = mock(JwtUtil.class);
        service = new JwtUtilsService(jwtUtil);
    }

    @Test
    void generateToken_delegates() {
        when(jwtUtil.generateToken("user")).thenReturn("token");

        String token = service.generateToken("user");

        assertThat(token).isEqualTo("token");
    }
}

