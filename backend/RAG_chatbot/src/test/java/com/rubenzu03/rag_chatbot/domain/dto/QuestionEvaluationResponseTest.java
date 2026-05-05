package com.rubenzu03.rag_chatbot.domain.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QuestionEvaluationResponseTest {

    @Test
    void testQuestionEvaluationResponseCreationWithArguments() {
        QuestionEvaluationResponse response = new QuestionEvaluationResponse("CORRECT", "The answer matches the criteria");

        assertThat(response.getResult()).isEqualTo("CORRECT");
        assertThat(response.getExplanation()).isEqualTo("The answer matches the criteria");
    }

    @Test
    void testQuestionEvaluationResponseCreationWithoutArguments() {
        QuestionEvaluationResponse response = new QuestionEvaluationResponse();

        assertThat(response.getResult()).isNull();
        assertThat(response.getExplanation()).isNull();
    }

    @Test
    void testQuestionEvaluationResponseSetters() {
        QuestionEvaluationResponse response = new QuestionEvaluationResponse();
        response.setResult("INCORRECT");
        response.setExplanation("The answer does not match");

        assertThat(response.getResult()).isEqualTo("INCORRECT");
        assertThat(response.getExplanation()).isEqualTo("The answer does not match");
    }

    @Test
    void testQuestionEvaluationResponseWithPartialMatch() {
        QuestionEvaluationResponse response = new QuestionEvaluationResponse("PARTIAL", "Some parts are correct");
        assertThat(response.getResult()).isEqualTo("PARTIAL");
        assertThat(response.getExplanation()).isEqualTo("Some parts are correct");
    }

    @Test
    void testQuestionEvaluationResponseFieldUpdate() {
        QuestionEvaluationResponse response = new QuestionEvaluationResponse("ORIGINAL", "Original explanation");
        response.setResult("UPDATED");
        response.setExplanation("Updated explanation");

        assertThat(response.getResult()).isEqualTo("UPDATED");
        assertThat(response.getExplanation()).isEqualTo("Updated explanation");
    }

    @Test
    void testQuestionEvaluationResponseWithEmptyString() {
        QuestionEvaluationResponse response = new QuestionEvaluationResponse("", "");

        assertThat(response.getResult()).isEmpty();
        assertThat(response.getExplanation()).isEmpty();
    }

    @Test
    void testQuestionEvaluationResponseWithNullValues() {
        QuestionEvaluationResponse response = new QuestionEvaluationResponse(null, null);

        assertThat(response.getResult()).isNull();
        assertThat(response.getExplanation()).isNull();
    }

    @Test
    void testQuestionEvaluationResponseWithDetailedExplanation() {
        String detailedExplanation = """
                The answer was evaluated based on:
                1. Accuracy of facts
                2. Relevance to the question
                3. Completeness of the response
                """;

        QuestionEvaluationResponse response = new QuestionEvaluationResponse("CORRECT", detailedExplanation);
        assertThat(response.getExplanation()).contains("Accuracy", "Relevance", "Completeness");
    }
}
