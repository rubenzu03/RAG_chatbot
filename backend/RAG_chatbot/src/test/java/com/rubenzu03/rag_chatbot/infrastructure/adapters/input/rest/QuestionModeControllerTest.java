package com.rubenzu03.rag_chatbot.infrastructure.adapters.input.rest;

import com.rubenzu03.rag_chatbot.application.ports.input.QuestionUseCase;
import com.rubenzu03.rag_chatbot.domain.dto.QuestionEvaluationRequest;
import com.rubenzu03.rag_chatbot.domain.dto.QuestionEvaluationResponse;
import com.rubenzu03.rag_chatbot.domain.dto.QuestionResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class QuestionModeControllerTest {

    @Test
    void generateQuestion_delegatesToUseCase() {
        QuestionUseCase useCase = mock(QuestionUseCase.class);
        QuestionModeController controller = new QuestionModeController(useCase);
        QuestionResponse response = new QuestionResponse("q-1", "What is RAG?");
        when(useCase.generateQuestion()).thenReturn(response);

        var result = controller.generateQuestion();

        assertThat(result.getBody()).isSameAs(response);
        verify(useCase).generateQuestion();
    }

    @Test
    void evaluateAnswer_delegatesToUseCase() {
        QuestionUseCase useCase = mock(QuestionUseCase.class);
        QuestionModeController controller = new QuestionModeController(useCase);
        QuestionEvaluationRequest request = new QuestionEvaluationRequest("q-1", "answer");
        QuestionEvaluationResponse response = new QuestionEvaluationResponse("CORRECT", "ok");
        when(useCase.evaluateAnswer(request)).thenReturn(response);

        var result = controller.evaluateAnswer(request);

        assertThat(result.getBody()).isSameAs(response);
        verify(useCase).evaluateAnswer(request);
    }
}

