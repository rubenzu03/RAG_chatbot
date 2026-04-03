package com.rubenzu03.rag_chatbot.infrastructure.adapters.output.chat;

import com.rubenzu03.rag_chatbot.infrastructure.security.ChatHistoryEncryptionService;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.List;
import java.util.stream.Collectors;

public class EncryptedChatMemory implements ChatMemory {

    private final ChatMemory delegate;
    private final ChatHistoryEncryptionService encryptionService;

    public EncryptedChatMemory(ChatMemory delegate, ChatHistoryEncryptionService encryptionService) {
        this.delegate = delegate;
        this.encryptionService = encryptionService;
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        List<Message> encryptedMessages = messages.stream()
                .map(message -> encryptMessage(message, conversationId))
                .collect(Collectors.toList());
        delegate.add(conversationId, encryptedMessages);
    }

    @Override
    public List<Message> get(String conversationId) {
        List<Message> encryptedMessages = delegate.get(conversationId);
        return encryptedMessages.stream()
                .map(message -> decryptMessage(message, conversationId))
                .collect(Collectors.toList());
    }

    @Override
    public void clear(String conversationId) {
        delegate.clear(conversationId);
    }

    private Message encryptMessage(Message message, String conversationId) {
        String encryptedContent = encryptionService.encrypt(message.getText(), conversationId);
        return createMessage(message, encryptedContent);
    }

    private Message decryptMessage(Message message, String conversationId) {
        String decryptedContent = encryptionService.decrypt(message.getText(), conversationId);
        return createMessage(message, decryptedContent);
    }

    private Message createMessage(Message original, String newContent) {
        MessageType type = original.getMessageType();
        switch (type) {
            case USER:
                return new UserMessage(newContent);
            case ASSISTANT:
                return new AssistantMessage(newContent);
            case SYSTEM:
                return new SystemMessage(newContent);
            default:
                return original;
        }
    }
}
