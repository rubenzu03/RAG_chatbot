package com.rubenzu03.rag_chatbot.application.service;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatHistoryService {

    private final ChatMemory chatMemory;

    public ChatHistoryService(ChatMemory chatMemory) {
        this.chatMemory = chatMemory;
    }

    public List<Message> getChatHistory(String userId) {
        return chatMemory.get(userId);
    }

    public void addUserMessage(String userId, Message message) {
        chatMemory.add(userId, message);
    }

    public void addAssistantMessage(String userId, Message message) {
        chatMemory.add(userId, message);
    }


}
