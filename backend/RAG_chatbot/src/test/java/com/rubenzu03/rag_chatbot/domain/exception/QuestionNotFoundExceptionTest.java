package com.rubenzu03.rag_chatbot.domain.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QuestionNotFoundExceptionTest {

    @Test
    void constructor_setsMessage() {
        QuestionNotFoundException exception = new QuestionNotFoundException("Question not found");

        assertThat(exception.getMessage()).isEqualTo("Question not found");
    }

    @Test
    void extendsRuntimeException() {
        QuestionNotFoundException exception = new QuestionNotFoundException("message");

        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
