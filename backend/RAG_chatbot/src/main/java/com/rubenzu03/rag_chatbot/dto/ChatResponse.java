package com.rubenzu03.rag_chatbot.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChatResponse {
    private String response;
    private String sessionId;

    public ChatResponse() {
    }

    public ChatResponse(String response, String sessionId) {
        this.response = response;
        this.sessionId = sessionId;
    }

}
