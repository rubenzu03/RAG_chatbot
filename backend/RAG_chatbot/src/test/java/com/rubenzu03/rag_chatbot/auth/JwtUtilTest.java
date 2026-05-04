package com.rubenzu03.rag_chatbot.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", "0123456789abcdef0123456789abcdef");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", 60000);
        jwtUtil.init();
    }

    @Test
    void generateAndParseToken() {
        String token = jwtUtil.generateToken("user");

        String username = jwtUtil.getUsernameFromToken(token);

        assertThat(username).isEqualTo("user");
        assertThat(jwtUtil.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_invalidTokenReturnsFalse() {
        assertThat(jwtUtil.validateToken("invalid.token.value")).isFalse();
    }
}
