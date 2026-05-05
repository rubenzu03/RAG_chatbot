package com.rubenzu03.rag_chatbot.auth;

import com.rubenzu03.rag_chatbot.application.service.UserDetailsService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebSecurityConfigTest {

    private WebSecurityConfig webSecurityConfig;

    @Mock
    private AuthEntryPoint authEntryPoint;

    @Mock
    private UserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        webSecurityConfig = new WebSecurityConfig(authEntryPoint, userDetailsService);
    }

    @Test
    void testAuthenticationJwtTokenFilterBeanCreation() {
        AuthTokenFilter filter = webSecurityConfig.authenticationJwtTokenFilter();
        assertThat(filter).isNotNull();
        assertThat(filter).isInstanceOf(AuthTokenFilter.class);
    }

    @Test
    void testAuthenticationManagerBeanCreation() throws Exception {
        AuthenticationConfiguration authConfig = mock(AuthenticationConfiguration.class);
        AuthenticationManager authManager = mock(AuthenticationManager.class);
        when(authConfig.getAuthenticationManager()).thenReturn(authManager);

        AuthenticationManager result = webSecurityConfig.authenticationManager(authConfig);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(authManager);
    }

    @Test
    void testPasswordEncoderBeanCreation() {
        PasswordEncoder encoder = webSecurityConfig.passwordEncoder();
        assertThat(encoder).isNotNull();
    }

    @Test
    void testPasswordEncoderIsNotNull() {
        PasswordEncoder encoder = webSecurityConfig.passwordEncoder();
        assertThat(encoder).isNotNull();
    }

    @Test
    void testPasswordEncoderCanEncodePassword() {
        PasswordEncoder encoder = webSecurityConfig.passwordEncoder();
        String password = "testPassword123";
        String encoded = encoder.encode(password);

        assertThat(encoded).isNotNull();
        assertThat(encoded).isNotEmpty();
        assertThat(encoded).isNotEqualTo(password);
    }

    @Test
    void testCorsConfigurationSourceBeanCreation() {
        CorsConfigurationSource corsSource = webSecurityConfig.corsConfigurationSource();
        assertThat(corsSource).isNotNull();
    }

    @Test
    void testCorsConfigurationSourceReturnsValid() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        CorsConfigurationSource corsSource = webSecurityConfig.corsConfigurationSource();

        assertThat(corsSource).isNotNull();
        assertThat(corsSource.getCorsConfiguration(request)).isNotNull();
    }

    @Test
    void testCorsAllowsLocalhost() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        CorsConfigurationSource corsSource = webSecurityConfig.corsConfigurationSource();

        assertThat(corsSource).isNotNull();
        var corsConfig = corsSource.getCorsConfiguration(request);
        Assertions.assertNotNull(corsConfig);
        assertThat(corsConfig.getAllowedOrigins()).contains("http://localhost:5173");
    }

    @Test
    void testCorsAllowsHttpMethods() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        CorsConfigurationSource corsSource = webSecurityConfig.corsConfigurationSource();

        var corsConfig = corsSource.getCorsConfiguration(request);
        Assertions.assertNotNull(corsConfig);
        assertThat(corsConfig.getAllowedMethods()).containsExactlyInAnyOrder("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }

    @Test
    void testCorsAllowsCredentials() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        CorsConfigurationSource corsSource = webSecurityConfig.corsConfigurationSource();

        var corsConfig = corsSource.getCorsConfiguration(request);
        Assertions.assertNotNull(corsConfig);
        assertThat(corsConfig.getAllowCredentials()).isTrue();
    }
}
