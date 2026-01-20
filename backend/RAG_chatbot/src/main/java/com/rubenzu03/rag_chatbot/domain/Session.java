package com.rubenzu03.rag_chatbot.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Setter
@Entity(name = "sessions")
public class Session {
    @Id
    private String sessionId;
    private Timestamp createdAt;
    private Timestamp lastAccessedAt;

    @PrePersist
    public void prePersist() {
        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();
        }
    }

    public void setSessionIdOverride(String sessionId) {
        this.sessionId = sessionId;
    }

}
