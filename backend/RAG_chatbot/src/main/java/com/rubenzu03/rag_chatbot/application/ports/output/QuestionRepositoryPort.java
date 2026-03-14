package com.rubenzu03.rag_chatbot.application.ports.output;

import com.rubenzu03.rag_chatbot.domain.model.Question;
import java.util.List;

public interface QuestionRepositoryPort {
    Question save(Question question);
    List<Question> findAll();
}

