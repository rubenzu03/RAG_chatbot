package com.rubenzu03.rag_chatbot.domain.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QuestionResponseTest {

    @Test
    void testQuestionResponseCreationWithArguments() {
        QuestionResponse response = new QuestionResponse("q-1", "What is RAG?");

        assertThat(response.getQuestionId()).isEqualTo("q-1");
        assertThat(response.getQuestion()).isEqualTo("What is RAG?");
    }

    @Test
    void testQuestionResponseCreationWithoutArguments() {
        QuestionResponse response = new QuestionResponse();

        assertThat(response.getQuestionId()).isNull();
        assertThat(response.getQuestion()).isNull();
    }

    @Test
    void testQuestionResponseSetters() {
        QuestionResponse response = new QuestionResponse();
        response.setQuestionId("q-2");
        response.setQuestion("How does AI work?");

        assertThat(response.getQuestionId()).isEqualTo("q-2");
        assertThat(response.getQuestion()).isEqualTo("How does AI work?");
    }

    @Test
    void testQuestionResponseWithDifferentIds() {
        QuestionResponse response1 = new QuestionResponse("q-100", "First question");
        QuestionResponse response2 = new QuestionResponse("q-101", "Second question");

        assertThat(response1.getQuestionId()).isNotEqualTo(response2.getQuestionId());
        assertThat(response1.getQuestion()).isNotEqualTo(response2.getQuestion());
    }

    @Test
    void testQuestionResponseFieldUpdate() {
        QuestionResponse response = new QuestionResponse("original-id", "Original question");
        response.setQuestionId("updated-id");
        response.setQuestion("Updated question");

        assertThat(response.getQuestionId()).isEqualTo("updated-id");
        assertThat(response.getQuestion()).isEqualTo("Updated question");
    }

    @Test
    void testQuestionResponseWithEmptyString() {
        QuestionResponse response = new QuestionResponse("", "");

        assertThat(response.getQuestionId()).isEmpty();
        assertThat(response.getQuestion()).isEmpty();
    }

    @Test
    void testQuestionResponseWithNullValues() {
        QuestionResponse response = new QuestionResponse(null, null);

        assertThat(response.getQuestionId()).isNull();
        assertThat(response.getQuestion()).isNull();
    }
}
