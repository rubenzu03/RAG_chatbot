package com.rubenzu03.rag_chatbot.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class AIService {

    private final ChatClient chatClient;

    @Autowired
    public AIService(@Qualifier("llama3ChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String simpleQueryTest(String query){
        return this.chatClient.prompt(query).call().content();
    }



}
