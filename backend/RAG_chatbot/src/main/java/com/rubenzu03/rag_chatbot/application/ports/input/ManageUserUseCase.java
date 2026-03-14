package com.rubenzu03.rag_chatbot.application.ports.input;

import com.rubenzu03.rag_chatbot.domain.dto.UserDto;

public interface ManageUserUseCase {
    UserDto registerUser(UserDto userDto);
    UserDto getUserProfile(Long id);
    boolean checkUserExists(String email);
}

