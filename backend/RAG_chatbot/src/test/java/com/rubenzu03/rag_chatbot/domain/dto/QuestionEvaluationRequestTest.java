package com.rubenzu03.rag_chatbot.domain.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QuestionEvaluationRequestTest {

    @Test
    void testQuestionEvaluationRequestCreationWithArguments() {
        QuestionEvaluationRequest request = new QuestionEvaluationRequest("q-1", "Yes, it's correct");

        assertThat(request.getQuestionId()).isEqualTo("q-1");
        assertThat(request.getAnswer()).isEqualTo("Yes, it's correct");
    }

    @Test
    void testQuestionEvaluationRequestCreationWithoutArguments() {
        QuestionEvaluationRequest request = new QuestionEvaluationRequest();

        assertThat(request.getQuestionId()).isNull();
        assertThat(request.getAnswer()).isNull();
    }

    @Test
    void testQuestionEvaluationRequestSetters() {
        QuestionEvaluationRequest request = new QuestionEvaluationRequest();
        request.setQuestionId("q-2");
        request.setAnswer("No, it's incorrect");

        assertThat(request.getQuestionId()).isEqualTo("q-2");
        assertThat(request.getAnswer()).isEqualTo("No, it's incorrect");
    }

    @Test
    void testQuestionEvaluationRequestWithDifferentAnswers() {
        QuestionEvaluationRequest request1 = new QuestionEvaluationRequest("q-1", "Correct");
        QuestionEvaluationRequest request2 = new QuestionEvaluationRequest("q-1", "Incorrect");

        assertThat(request1.getAnswer()).isNotEqualTo(request2.getAnswer());
    }

    @Test
    void testQuestionEvaluationRequestFieldUpdate() {
        QuestionEvaluationRequest request = new QuestionEvaluationRequest("q-orig", "Original answer");
        request.setQuestionId("q-updated");
        request.setAnswer("Updated answer");

        assertThat(request.getQuestionId()).isEqualTo("q-updated");
        assertThat(request.getAnswer()).isEqualTo("Updated answer");
    }

    @Test
    void testQuestionEvaluationRequestWithEmptyString() {
        QuestionEvaluationRequest request = new QuestionEvaluationRequest("", "");

        assertThat(request.getQuestionId()).isEmpty();
        assertThat(request.getAnswer()).isEmpty();
    }

    @Test
    void testQuestionEvaluationRequestWithMultilineAnswer() {
        String multilineAnswer = "Line 1\nLine 2\nLine 3";
        QuestionEvaluationRequest request = new QuestionEvaluationRequest("q-1", multilineAnswer);

        assertThat(request.getAnswer()).contains("Line 1", "Line 2", "Line 3");
    }
}
