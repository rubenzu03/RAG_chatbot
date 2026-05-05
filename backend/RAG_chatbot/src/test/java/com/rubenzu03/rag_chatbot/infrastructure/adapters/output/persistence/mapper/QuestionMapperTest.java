package com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.mapper;

import com.rubenzu03.rag_chatbot.domain.model.QuestionDTO;
import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.entity.QuestionEntity;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;

class QuestionMapperTest {

    private final QuestionMapper mapper = new QuestionMapper();

    @Test
    void toEntity_mapsAllFields() {
        QuestionDTO dto = new QuestionDTO();
        dto.setId("q-1");
        dto.setQuestion("What?");
        dto.setContext("ctx");
        Timestamp created = new Timestamp(System.currentTimeMillis());
        dto.setCreatedAt(created);

        QuestionEntity entity = mapper.toEntity(dto);

        assertThat(entity.getId()).isEqualTo("q-1");
        assertThat(entity.getQuestion()).isEqualTo("What?");
        assertThat(entity.getContext()).isEqualTo("ctx");
        assertThat(entity.getCreatedAt()).isEqualTo(created);
    }

    @Test
    void toDomain_mapsQuestionContextAndCreatedAt() {
        QuestionEntity entity = new QuestionEntity();
        entity.setId("q-2");
        entity.setQuestion("Why?");
        entity.setContext("context");
        Timestamp created = new Timestamp(System.currentTimeMillis());
        entity.setCreatedAt(created);

        QuestionDTO dto = mapper.toDomain(entity);

        assertThat(dto.getQuestion()).isEqualTo("Why?");
        assertThat(dto.getContext()).isEqualTo("context");
        assertThat(dto.getCreatedAt()).isEqualTo(created);
    }
}

