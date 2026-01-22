package com.rubenzu03.rag_chatbot.components;

import com.rubenzu03.rag_chatbot.domain.Session;
import com.rubenzu03.rag_chatbot.persistence.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChatDeletionOverTime {

    private static final long TWO_HOURS = 2 * 60 * 60 * 1000;

    private final SessionRepository sessionRepository;
    private List<Session> sessions;
    
    @Autowired
    public ChatDeletionOverTime(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
        sessions = sessionRepository.findAll();

    }

    @Scheduled(fixedRate = TWO_HOURS)
    private void deleteChatHistory(){
        sessions = sessionRepository.findAll();
        sessions.forEach(session -> {
            if(session.getLastAccessedAt().getTime() < System.currentTimeMillis() - TWO_HOURS){
                sessionRepository.deleteChatMemoryBySessionId(session.getSessionId());
            }
        });
    }

}
