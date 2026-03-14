package com.rubenzu03.rag_chatbot.application.ports.input;

import com.rubenzu03.rag_chatbot.domain.model.Question;

public interface QuestionUseCase {
    Question generateQuestion();
    void saveGeneratedQuestion(Question question);
}

