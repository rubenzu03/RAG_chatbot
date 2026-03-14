package com.rubenzu03.rag_chatbot.application.ports.input;

import com.rubenzu03.rag_chatbot.domain.model.User;

public interface ManageUserUseCase {
    User registerUser(User user);
    User getUserProfile(Long id);
    boolean checkUserExists(String email);
}

