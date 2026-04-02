package com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.adapter;

import com.rubenzu03.rag_chatbot.application.ports.output.QuestionRepositoryPort;
import com.rubenzu03.rag_chatbot.domain.model.QuestionDTO;
import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.GeneratedQuestionRepository;
import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.entity.QuestionEntity;
import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.mapper.QuestionMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class QuestionPersistenceAdapter implements QuestionRepositoryPort {

    private final GeneratedQuestionRepository generatedQuestionRepository;
    private final QuestionMapper questionMapper;

    public QuestionPersistenceAdapter(GeneratedQuestionRepository generatedQuestionRepository, QuestionMapper questionMapper) {
        this.generatedQuestionRepository = generatedQuestionRepository;
        this.questionMapper = questionMapper;
    }

    @Override
    public Optional<QuestionEntity> save(QuestionDTO questionEntity) {
        QuestionEntity saved = generatedQuestionRepository.save(questionMapper.toEntity(questionEntity));
        return Optional.of(saved);
    }

    @Override
    public Optional<QuestionDTO> findById(String id) {
        return Optional.of(generatedQuestionRepository.findById(id)
                .map(questionMapper::toDomain)
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + id)));
    }

    @Override
    public void deleteAll() {
        generatedQuestionRepository.deleteAll();
    }
}
