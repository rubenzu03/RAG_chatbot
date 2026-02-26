package com.rubenzu03.rag_chatbot.components;

import com.rubenzu03.rag_chatbot.persistence.ChatMemoryRepository;
import com.rubenzu03.rag_chatbot.persistence.SessionRepository;
import com.rubenzu03.rag_chatbot.service.ChatMemoryService;
import com.rubenzu03.rag_chatbot.service.QuestionModeService;
import com.rubenzu03.rag_chatbot.service.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class DeleteDataOnStop {

    private final Logger logger = LoggerFactory.getLogger(DeleteDataOnStop.class);

    private final SessionService sessionService;
    private final ChatMemoryService chatMemoryService;
    private final QuestionModeService questionModeService;

    @Autowired
    public DeleteDataOnStop(SessionService sessionService, ChatMemoryService chatMemoryService,
                            QuestionModeService questionModeService) {
        this.sessionService = sessionService;
        this.chatMemoryService = chatMemoryService;
        this.questionModeService = questionModeService;
    }

    @EventListener(ContextClosedEvent.class)
    public void onContextClosed() {
        sessionService.deleteAllSessions();
        chatMemoryService.deleteAll();
        questionModeService.deleteAllQuestions();
        logger.info("Deleting all chat memory on shutdown");
    }
}
