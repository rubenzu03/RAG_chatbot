package com.rubenzu03.rag_chatbot.application.ports.output;

import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.entity.QuestionEntity;
import com.rubenzu03.rag_chatbot.domain.model.QuestionDTO;
import java.util.Optional;

public interface QuestionRepositoryPort {
    Optional<QuestionEntity> save(QuestionDTO questionEntity);
    Optional<QuestionDTO> findById(String id);
    void deleteAll();
}

