package com.rubenzu03.rag_chatbot.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.util.UUID;

@Entity
public class Session {
    @Id
    private final String sessionId = UUID.randomUUID().toString();
}
