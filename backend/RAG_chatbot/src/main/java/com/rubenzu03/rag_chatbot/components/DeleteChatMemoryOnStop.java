package com.rubenzu03.rag_chatbot.components;

import com.rubenzu03.rag_chatbot.persistence.ChatMemoryRepository;
import com.rubenzu03.rag_chatbot.persistence.SessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class DeleteChatMemoryOnStop {

    private final Logger logger = LoggerFactory.getLogger(DeleteChatMemoryOnStop.class);

    private final SessionRepository sessionRepository;
    private final ChatMemoryRepository chatMemoryRepository;

    @Autowired
    public DeleteChatMemoryOnStop(SessionRepository sessionRepository, ChatMemoryRepository chatMemoryRepository) {
        this.sessionRepository = sessionRepository;
        this.chatMemoryRepository = chatMemoryRepository;
    }

    @EventListener(ContextClosedEvent.class)
    public void onContextClosed() {
        logger.info("Deleting all chat memory on shutdown");
        chatMemoryRepository.deleteAll();
    }
}
