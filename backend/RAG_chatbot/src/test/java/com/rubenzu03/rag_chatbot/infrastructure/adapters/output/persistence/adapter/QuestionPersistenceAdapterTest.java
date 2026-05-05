package com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.adapter;

import com.rubenzu03.rag_chatbot.domain.model.QuestionDTO;
import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.GeneratedQuestionRepository;
import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.entity.QuestionEntity;
import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.mapper.QuestionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class QuestionPersistenceAdapterTest {

    private GeneratedQuestionRepository repository;
    private QuestionMapper mapper;
    private QuestionPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = mock(GeneratedQuestionRepository.class);
        mapper = new QuestionMapper();
        adapter = new QuestionPersistenceAdapter(repository, mapper);
    }

    @Test
    void save_persistsMappedEntity() {
        QuestionDTO dto = new QuestionDTO();
        dto.setId("q-1");
        dto.setQuestion("What?");
        dto.setContext("ctx");
        Timestamp createdAt = new Timestamp(System.currentTimeMillis());
        dto.setCreatedAt(createdAt);

        when(repository.save(org.mockito.ArgumentMatchers.any(QuestionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Optional<QuestionEntity> result = adapter.save(dto);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("q-1");
        assertThat(result.get().getQuestion()).isEqualTo("What?");
        assertThat(result.get().getContext()).isEqualTo("ctx");
        assertThat(result.get().getCreatedAt()).isEqualTo(createdAt);
        verify(repository).save(org.mockito.ArgumentMatchers.any(QuestionEntity.class));
    }

    @Test
    void findById_returnsMappedDto() {
        QuestionEntity entity = new QuestionEntity();
        entity.setId("q-1");
        entity.setQuestion("What?");
        entity.setContext("ctx");
        when(repository.findById("q-1")).thenReturn(Optional.of(entity));

        Optional<QuestionDTO> result = adapter.findById("q-1");

        assertThat(result).isPresent();
        assertThat(result.get().getQuestion()).isEqualTo("What?");
        assertThat(result.get().getContext()).isEqualTo("ctx");
    }

    @Test
    void findById_throwsWhenMissing() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adapter.findById("missing"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Question not found with id: missing");
    }

    @Test
    void deleteAll_delegates() {
        adapter.deleteAll();
        verify(repository).deleteAll();
    }
}
