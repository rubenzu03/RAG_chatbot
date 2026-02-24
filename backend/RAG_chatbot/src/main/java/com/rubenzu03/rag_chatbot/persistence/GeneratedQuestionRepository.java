package com.rubenzu03.rag_chatbot.persistence;

import com.rubenzu03.rag_chatbot.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GeneratedQuestionRepository extends JpaRepository<Question, String> {
}

