package com.rubenzu03.rag_chatbot.application.ports.input;

import com.rubenzu03.rag_chatbot.domain.dto.QuestionEvaluationRequest;
import com.rubenzu03.rag_chatbot.domain.dto.QuestionEvaluationResponse;
import com.rubenzu03.rag_chatbot.domain.dto.QuestionResponse;

public interface QuestionUseCase {
    QuestionResponse generateQuestion();
    QuestionEvaluationResponse evaluateAnswer(QuestionEvaluationRequest questionEvaluationRequest);
}

