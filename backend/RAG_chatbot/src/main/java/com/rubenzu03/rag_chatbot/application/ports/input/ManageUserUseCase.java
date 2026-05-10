package com.rubenzu03.rag_chatbot.application.ports.input;

import com.rubenzu03.rag_chatbot.domain.model.UserDTO;

public interface ManageUserUseCase {
    UserDTO registerUser(UserDTO userDto);
    UserDTO getUserProfile(Long id);
    String login(String email, String password);
    boolean checkUserExists(String email);
    void deleteUser(String email);
}
