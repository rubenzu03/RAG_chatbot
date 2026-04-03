package com.rubenzu03.rag_chatbot.infrastructure.adapters.output.chat;

import com.rubenzu03.rag_chatbot.infrastructure.security.ChatHistoryEncryptionService;
import com.rubenzu03.rag_chatbot.infrastructure.security.ConversationEncryptionKeyService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EncryptedChatMemoryTest {

    @Test
    void shouldRoundTripMessagesUsingConversationScopedKey() {
        ConversationEncryptionKeyService keyService = mock(ConversationEncryptionKeyService.class);
        when(keyService.getOrCreateKey("userA::default")).thenReturn(fixedKey());

        ChatHistoryEncryptionService encryptionService = new ChatHistoryEncryptionService(keyService);
        ChatMemory delegate = new InMemoryChatMemory();
        EncryptedChatMemory encryptedMemory = new EncryptedChatMemory(delegate, encryptionService);

        List<Message> toPersist = List.of(new UserMessage("hola"));
        encryptedMemory.add("userA::default", toPersist);

        List<Message> recovered = encryptedMemory.get("userA::default");
        assertEquals(1, recovered.size());
        assertEquals("hola", recovered.get(0).getText());
    }

    private byte[] fixedKey() {
        byte[] key = new byte[32];
        for (int i = 0; i < key.length; i++) {
            key[i] = 7;
        }
        return key;
    }

    private static class InMemoryChatMemory implements ChatMemory {
        private final Map<String, List<Message>> store = new HashMap<>();

        @Override
        public void add(String conversationId, List<Message> messages) {
            store.computeIfAbsent(conversationId, k -> new ArrayList<>()).addAll(messages);
        }

        @Override
        public List<Message> get(String conversationId) {
            return new ArrayList<>(store.getOrDefault(conversationId, List.of()));
        }

        @Override
        public void clear(String conversationId) {
            store.remove(conversationId);
        }
    }
}

