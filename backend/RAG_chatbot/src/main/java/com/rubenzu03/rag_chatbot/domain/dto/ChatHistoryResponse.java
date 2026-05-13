package com.rubenzu03.rag_chatbot.domain.dto;

import java.util.List;

public record ChatHistoryResponse(List<ChatHistoryMessage> history) {
}