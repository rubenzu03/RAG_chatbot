package com.rubenzu03.rag_chatbot.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChatResponse {
    private String response;
    private String userId;

    public ChatResponse() {
    }

    public ChatResponse(String response, String userId) {
        this.response = response;
        this.userId = userId;
    }

}