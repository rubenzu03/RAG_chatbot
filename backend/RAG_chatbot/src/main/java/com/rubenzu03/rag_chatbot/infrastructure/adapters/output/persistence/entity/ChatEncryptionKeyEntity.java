package com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "chat_encryption_keys")
public class ChatEncryptionKeyEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private String conversationId;

    @Column(nullable = false, length = 255)
    private String keyMaterial;

    @Column(nullable = false)
    private Timestamp createdAt;
}

