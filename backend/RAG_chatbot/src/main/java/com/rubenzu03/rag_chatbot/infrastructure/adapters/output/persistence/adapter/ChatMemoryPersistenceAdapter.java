package com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.adapter;

import com.rubenzu03.rag_chatbot.application.ports.output.ChatMemoryPort;
import com.rubenzu03.rag_chatbot.domain.dto.ChatHistoryMessage;
import com.rubenzu03.rag_chatbot.infrastructure.security.ChatHistoryEncryptionService;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class ChatMemoryPersistenceAdapter implements ChatMemoryPort {

    private final JdbcChatMemoryRepository chatMemoryRepository;
    private final ChatMemory chatMemory;
    private final ChatHistoryEncryptionService encryptionService;

    public ChatMemoryPersistenceAdapter(
            JdbcChatMemoryRepository chatMemoryRepository,
            ChatMemory chatMemory,
            ChatHistoryEncryptionService encryptionService) {
        this.chatMemoryRepository = chatMemoryRepository;
        this.chatMemory = chatMemory;
        this.encryptionService = encryptionService;
    }

    @Override
    public void deleteAllHistory() {
        chatMemoryRepository.findConversationIds().forEach(chatMemoryRepository::deleteByConversationId);
    }

    @Override
    public void deleteHistoryById(String userId) {
        chatMemoryRepository.deleteByConversationId(userId);
    }

    @Override
    public List<ChatHistoryMessage> getHistory(String userId) {
        return chatMemory.get(userId).stream()
                .map(ChatMemoryPersistenceAdapter::toHistoryMessage)
                .toList();
    }

    private static ChatHistoryMessage toHistoryMessage(Message message) {
        String role = switch (message) {
            case UserMessage ignored -> "user";
            case AssistantMessage ignored -> "assistant";
            case SystemMessage ignored -> "system";
            case ToolResponseMessage ignored -> "tool";
            default -> message.getMessageType().name().toLowerCase(Locale.ROOT);
        };

        return new ChatHistoryMessage(role, message.getText());
    }
}
