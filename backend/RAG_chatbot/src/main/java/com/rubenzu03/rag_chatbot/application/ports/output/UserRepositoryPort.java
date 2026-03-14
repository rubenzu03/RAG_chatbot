package com.rubenzu03.rag_chatbot.application.ports.output;

import com.rubenzu03.rag_chatbot.domain.model.User;
import java.util.Optional;

public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    boolean existsByEmail(String email);
}

