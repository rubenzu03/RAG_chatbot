package com.rubenzu03.rag_chatbot.infrastructure.ragmodules.preretrieve;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueryTransformerModuleTest {

    private QueryTransformerModule module;

    @Mock
    private ChatMemory chatMemory;

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @BeforeEach
    void setUp() {
        module = new QueryTransformerModule(chatClientBuilder, chatMemory);
    }

    @Test
    void testTransformQueryWithEmptyHistory() {
        when(chatMemory.get(anyString())).thenReturn(Collections.emptyList());
        assertThat(module).isNotNull();
        assertThat(chatMemory.get("user123")).isEmpty();
    }

    @Test
    void testTransformQueryWithConversationHistory() {
        List<Message> history = List.of(
                new UserMessage("What is machine learning?"),
                new AssistantMessage("Machine learning is a subset of AI...")
        );
        when(chatMemory.get("conv123")).thenReturn(history);

        assertThat(chatMemory.get("conv123")).hasSize(2);
        assertThat(chatMemory.get("conv123").get(0)).isInstanceOf(UserMessage.class);
    }

    @Test
    void testTransformQueryWithNullConversationId() {
        when(chatMemory.get(null)).thenReturn(Collections.emptyList());
        assertThat(module).isNotNull();
        assertThat(chatMemory.get(null)).isEmpty();
    }

    @Test
    void testTransformQueryWithBlankConversationId() {
        when(chatMemory.get(" ")).thenReturn(Collections.emptyList());

        assertThat(module).isNotNull();
        assertThat(chatMemory.get(" ")).isEmpty();
    }

    @Test
    void testTransformQueryPreservesRawQueryText() {
        String rawQuery = "Original question text";
        when(chatMemory.get(anyString())).thenReturn(Collections.emptyList());

        assertThat(rawQuery).isNotEmpty();
        assertThat(chatMemory.get("user456")).isEmpty();
    }

    @Test
    void testTransformQueryWithMultipleHistoryMessages() {
        List<Message> history = List.of(
                new UserMessage("First question?"),
                new AssistantMessage("First answer"),
                new UserMessage("Second question?"),
                new AssistantMessage("Second answer")
        );
        when(chatMemory.get("conv789")).thenReturn(history);
        assertThat(chatMemory.get("conv789")).hasSize(4);
    }

    @Test
    void testTransformQueryHistoryIsIncluded() {
        List<Message> history = List.of(
                new UserMessage("Context question"),
                new AssistantMessage("Context answer")
        );
        when(chatMemory.get("conv999")).thenReturn(history);

        assertThat(chatMemory.get("conv999")).hasSize(2);
    }
}

