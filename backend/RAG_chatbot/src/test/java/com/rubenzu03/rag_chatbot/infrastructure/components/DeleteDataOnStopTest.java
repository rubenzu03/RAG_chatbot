package com.rubenzu03.rag_chatbot.infrastructure.components;

import com.rubenzu03.rag_chatbot.application.service.ChatMemoryService;
import com.rubenzu03.rag_chatbot.application.service.QuestionModeService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DeleteDataOnStopTest {

    @Test
    void onContextClosed_deletesAllData() {
        ChatMemoryService chatMemoryService = mock(ChatMemoryService.class);
        QuestionModeService questionModeService = mock(QuestionModeService.class);
        DeleteDataOnStop component = new DeleteDataOnStop(chatMemoryService, questionModeService);

        component.onContextClosed();

        verify(chatMemoryService).deleteAll();
        verify(questionModeService).deleteAllQuestions();
    }
}

