package com.rubenzu03.rag_chatbot.service;

import com.rubenzu03.rag_chatbot.domain.Session;
import com.rubenzu03.rag_chatbot.persistence.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;

    @Autowired
    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }


    public Session getOrCreateSession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return createNewSession();
        }

        Session session = sessionRepository.findById(sessionId).orElse(null);
        if (session == null) {
            return createSessionWithId(sessionId);
        }

        updateLastAccessed(session);
        return session;
    }


    public Session createNewSession() {
        Session session = new Session();
        Timestamp now = Timestamp.from(Instant.now());
        session.setCreatedAt(now);
        session.setLastAccessedAt(now);
        return sessionRepository.save(session);
    }


    private Session createSessionWithId(String sessionId) {
        Session session = new Session();
        session.setSessionIdOverride(sessionId);
        Timestamp now = Timestamp.from(Instant.now());
        session.setCreatedAt(now);
        session.setLastAccessedAt(now);
        return sessionRepository.save(session);
    }

    private void updateLastAccessed(Session session) {
        session.setLastAccessedAt(Timestamp.from(Instant.now()));
        sessionRepository.save(session);
    }

    public Session getSessionById(String sessionId) {
        return sessionRepository.findById(sessionId).orElse(null);
    }

}
