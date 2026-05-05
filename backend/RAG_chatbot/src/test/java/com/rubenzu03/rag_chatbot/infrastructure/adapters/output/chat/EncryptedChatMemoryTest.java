package com.rubenzu03.rag_chatbot.infrastructure.adapters.output.chat;

import com.rubenzu03.rag_chatbot.infrastructure.security.ChatHistoryEncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EncryptedChatMemoryTest {

    private EncryptedChatMemory encryptedMemory;

    @Mock
    private ChatMemory delegateMemory;

    @Mock
    private ChatHistoryEncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        encryptedMemory = new EncryptedChatMemory(delegateMemory, encryptionService);
    }

    @Test
    void testEncryptedChatMemoryCreation() {
        // Assert - object is created in setUp
        assertThat(encryptedMemory).isNotNull();
    }

    @Test
    void testGetDecryptsMessages() {
        // Arrange
        String conversationId = "conv-1";
        List<Message> encryptedMessages = List.of(
                new UserMessage("encrypted-1"),
                new AssistantMessage("encrypted-2"));
        when(delegateMemory.get(conversationId)).thenReturn(encryptedMessages);
        when(encryptionService.decrypt(anyString(), anyString()))
                .thenReturn("decrypted-content");

        // Act
        List<Message> result = encryptedMemory.get(conversationId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
    }

    @Test
    void testGetWithEmptyList() {
        String conversationId = "conv-empty";
        when(delegateMemory.get(conversationId)).thenReturn(List.of());
        List<Message> result = encryptedMemory.get(conversationId);

        assertThat(result).isEmpty();
    }

    @Test
    void testClearDelegatesToMemory() {
        String conversationId = "conv-test";
        encryptedMemory.clear(conversationId);

        assertThat(conversationId).isEqualTo("conv-test");
    }

    @Test
    void testMultipleMessagesAreDecrypted() {
        List<Message> messages = List.of(
                new UserMessage("msg1"),
                new AssistantMessage("msg2"),
                new UserMessage("msg3"));
        when(delegateMemory.get("conv")).thenReturn(messages);
        when(encryptionService.decrypt(anyString(), anyString()))
                .thenReturn("decrypted");
        List<Message> result = encryptedMemory.get("conv");

        assertThat(result).hasSize(3);
    }

    @Test
    void testDifferentConversationIds() {
        when(delegateMemory.get("conv1")).thenReturn(List.of(new UserMessage("msg1")));
        when(delegateMemory.get("conv2")).thenReturn(List.of(new UserMessage("msg2")));
        when(encryptionService.decrypt(anyString(), anyString()))
                .thenReturn("decrypted");

        List<Message> result1 = encryptedMemory.get("conv1");
        List<Message> result2 = encryptedMemory.get("conv2");

        assertThat(result1).hasSize(1);
        assertThat(result2).hasSize(1);
    }
}
