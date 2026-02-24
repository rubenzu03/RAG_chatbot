package com.rubenzu03.rag_chatbot.service;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatHistoryService {

    //TODO: Change to User functionality when implemented
    private final ChatMemory chatMemory;

    public ChatHistoryService(ChatMemory chatMemory) {
        this.chatMemory = chatMemory;
    }

    public List<Message> getChatHistory(String sessionId) {
        return chatMemory.get(sessionId);
    }

    public void addUserMessage(String sessionId, Message message) {
        chatMemory.add(sessionId, message);
    }

    public void addAssistantMessage(String sessionId, Message message) {
        chatMemory.add(sessionId, message);
    }


}
