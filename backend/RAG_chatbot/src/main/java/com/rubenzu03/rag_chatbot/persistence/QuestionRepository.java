package com.rubenzu03.rag_chatbot.persistence;

import com.rubenzu03.rag_chatbot.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, String> {
}
