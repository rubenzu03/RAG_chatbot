package com.rubenzu03.rag_chatbot.infrastructure.adapters.input.rest;

import com.rubenzu03.rag_chatbot.application.ports.input.ManageUserUseCase;
import com.rubenzu03.rag_chatbot.domain.model.UserDTO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    @Test
    void authenticateUser_delegatesLogin() {
        ManageUserUseCase useCase = mock(ManageUserUseCase.class);
        AuthController controller = new AuthController(useCase);
        UserDTO user = new UserDTO();
        user.setEmail("user@example.com");
        user.setPassword("secret");
        when(useCase.login("user@example.com", "secret")).thenReturn("token");

        String response = controller.authenticateUser(user);

        assertThat(response).isEqualTo("token");
        verify(useCase).login("user@example.com", "secret");
    }

    @Test
    void registerUser_returnsBadRequestWhenUserExists() {
        ManageUserUseCase useCase = mock(ManageUserUseCase.class);
        AuthController controller = new AuthController(useCase);
        UserDTO user = new UserDTO();
        user.setEmail("user@example.com");
        when(useCase.checkUserExists("user@example.com")).thenReturn(true);

        var response = controller.registerUser(user);

        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        assertThat(response.getBody()).isEqualTo("User already exists");
    }

    @Test
    void registerUser_registersWhenUserDoesNotExist() {
        ManageUserUseCase useCase = mock(ManageUserUseCase.class);
        AuthController controller = new AuthController(useCase);
        UserDTO user = new UserDTO();
        user.setEmail("user@example.com");
        when(useCase.checkUserExists("user@example.com")).thenReturn(false);

        var response = controller.registerUser(user);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo("User registered successfully");
        verify(useCase).registerUser(user);
    }
}

