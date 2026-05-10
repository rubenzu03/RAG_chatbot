package com.rubenzu03.rag_chatbot.application.ports.output;

import com.rubenzu03.rag_chatbot.domain.model.UserDTO;

import java.util.Optional;

public interface UserRepositoryPort {
    UserDTO save(UserDTO user);
    Optional<UserDTO> findByEmail(String email);
    Optional<UserDTO> findById(Long id);
    boolean existsByEmail(String email);
    void deleteByEmail(String email);
}

