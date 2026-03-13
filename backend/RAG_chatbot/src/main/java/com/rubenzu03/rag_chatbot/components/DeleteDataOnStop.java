package com.rubenzu03.rag_chatbot.components;

import com.rubenzu03.rag_chatbot.service.ChatMemoryService;
import com.rubenzu03.rag_chatbot.service.QuestionModeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class DeleteDataOnStop {

    private final Logger logger = LoggerFactory.getLogger(DeleteDataOnStop.class);

    private final ChatMemoryService chatMemoryService;
    private final QuestionModeService questionModeService;

    @Autowired
    public DeleteDataOnStop(ChatMemoryService chatMemoryService,
                            QuestionModeService questionModeService) {
        this.chatMemoryService = chatMemoryService;
        this.questionModeService = questionModeService;
    }

    @EventListener(ContextClosedEvent.class)
    public void onContextClosed() {
        chatMemoryService.deleteAll();
        questionModeService.deleteAllQuestions();
        logger.info("Deleting all chat memory on shutdown");
    }
}
