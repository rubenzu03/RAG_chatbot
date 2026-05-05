package com.rubenzu03.rag_chatbot.domain.model;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;

class QuestionDTOTest {

    @Test
    void constructorWithoutArgs_initializesFieldsAsNull() {
        QuestionDTO question = new QuestionDTO();

        assertThat(question.getId()).isNull();
        assertThat(question.getQuestion()).isNull();
        assertThat(question.getContext()).isNull();
        assertThat(question.getCreatedAt()).isNull();
    }

    @Test
    void constructorWithAllArgs_setsAllFields() {
        Timestamp createdAt = Timestamp.valueOf("2026-05-05 14:00:00");

        QuestionDTO question = new QuestionDTO("q-1", "What is RAG?", "Context text", createdAt);

        assertThat(question.getId()).isEqualTo("q-1");
        assertThat(question.getQuestion()).isEqualTo("What is RAG?");
        assertThat(question.getContext()).isEqualTo("Context text");
        assertThat(question.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void setters_updateAllFields() {
        QuestionDTO question = new QuestionDTO();
        Timestamp createdAt = Timestamp.valueOf("2026-05-05 15:00:00");

        question.setId("q-2");
        question.setQuestion("How does retrieval work?");
        question.setContext("Retrieved chunks");
        question.setCreatedAt(createdAt);

        assertThat(question.getId()).isEqualTo("q-2");
        assertThat(question.getQuestion()).isEqualTo("How does retrieval work?");
        assertThat(question.getContext()).isEqualTo("Retrieved chunks");
        assertThat(question.getCreatedAt()).isEqualTo(createdAt);
    }
}
