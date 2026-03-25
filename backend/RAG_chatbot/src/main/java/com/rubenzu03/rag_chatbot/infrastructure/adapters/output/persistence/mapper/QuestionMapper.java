package com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.mapper;

import com.rubenzu03.rag_chatbot.domain.model.QuestionDTO;
import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.entity.QuestionEntity;
import org.springframework.stereotype.Component;

@Component
public class QuestionMapper {

    public QuestionDTO toDomain(QuestionEntity entity) {
        QuestionDTO questionDTO = new QuestionDTO();
        entity.setId(questionDTO.getId());
        questionDTO.setQuestion(entity.getQuestion());
        questionDTO.setContext(entity.getContext());
        questionDTO.setCreatedAt(entity.getCreatedAt());
        return questionDTO;
    }

    public QuestionEntity toEntity(QuestionDTO domain) {
        QuestionEntity entity = new QuestionEntity();
        entity.setId(domain.getId());
        entity.setQuestion(domain.getQuestion());
        entity.setContext(domain.getContext());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }
}
