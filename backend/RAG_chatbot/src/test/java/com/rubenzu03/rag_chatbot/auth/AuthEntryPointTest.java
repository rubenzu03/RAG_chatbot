package com.rubenzu03.rag_chatbot.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthEntryPointTest {

    private AuthEntryPoint authEntryPoint;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationException authException;

    @BeforeEach
    void setUp() {
        authEntryPoint = new AuthEntryPoint();
    }

    @Test
    void testCommenWithUnauthorizedException() throws Exception {
        authEntryPoint.commence(request, response, authException);
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }

    @Test
    void testCommenceReturnsUnauthorizedStatus() throws Exception {
        int expectedStatusCode = HttpServletResponse.SC_UNAUTHORIZED;
        authEntryPoint.commence(request, response, authException);

        verify(response).sendError(expectedStatusCode, "Unauthorized");
    }

    @Test
    void testCommenceWithDifferentException() throws Exception {
        AuthenticationException customException = new AuthenticationException("Custom auth error") {};
        authEntryPoint.commence(request, response, customException);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }

    @Test
    void testCommenceErrorMessage() throws Exception {
        authEntryPoint.commence(request, response, authException);
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }

    @Test
    void testCommenceWithTwoDifferentExceptions() throws Exception {
        AuthenticationException authEx1 = new AuthenticationException("Invalid token") {};
        AuthenticationException authEx2 = new AuthenticationException("Expired token") {};

        authEntryPoint.commence(request, response, authEx1);
        authEntryPoint.commence(request, response, authEx2);

        verify(response, times(2)).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
}

