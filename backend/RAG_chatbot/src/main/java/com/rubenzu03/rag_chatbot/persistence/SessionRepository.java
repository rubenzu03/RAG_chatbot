package com.rubenzu03.rag_chatbot.persistence;

import com.rubenzu03.rag_chatbot.domain.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<Session, String> {

}
