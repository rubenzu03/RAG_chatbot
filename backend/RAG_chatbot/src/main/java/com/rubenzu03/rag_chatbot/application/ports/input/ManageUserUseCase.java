package com.rubenzu03.rag_chatbot.application.ports.input;

import com.rubenzu03.rag_chatbot.domain.model.UserDTO;

public interface ManageUserUseCase {
    UserDTO registerUser(UserDTO userDto);
    UserDTO getUserProfile(Long id);
    boolean checkUserExists(String email);
}

