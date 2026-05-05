package com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.entity;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;

class ChatEncryptionKeyEntityTest {

    @Test
    void testChatEncryptionKeyEntityCreation() {
        ChatEncryptionKeyEntity entity = new ChatEncryptionKeyEntity();
        entity.setConversationId("conv-123");
        entity.setKeyMaterial("key123");
        entity.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        assertThat(entity.getConversationId()).isEqualTo("conv-123");
        assertThat(entity.getKeyMaterial()).isEqualTo("key123");
        assertThat(entity.getCreatedAt()).isNotNull();
    }

    @Test
    void testChatEncryptionKeySetters() {
        ChatEncryptionKeyEntity entity = new ChatEncryptionKeyEntity();
        entity.setConversationId("conv-456");
        entity.setKeyMaterial("secret-key");

        assertThat(entity.getConversationId()).isEqualTo("conv-456");
        assertThat(entity.getKeyMaterial()).isEqualTo("secret-key");
    }

    @Test
    void testChatEncryptionKeyCreatedAtTimestamp() {
        ChatEncryptionKeyEntity entity = new ChatEncryptionKeyEntity();
        Timestamp now = new Timestamp(System.currentTimeMillis());

        entity.setCreatedAt(now);
        assertThat(entity.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void testChatEncryptionKeyWithAllFields() {
        ChatEncryptionKeyEntity entity = new ChatEncryptionKeyEntity();
        Timestamp now = new Timestamp(System.currentTimeMillis());

        entity.setConversationId("full-conv");
        entity.setKeyMaterial("full-key");
        entity.setCreatedAt(now);

        assertThat(entity.getConversationId()).isEqualTo("full-conv");
        assertThat(entity.getKeyMaterial()).isEqualTo("full-key");
        assertThat(entity.getCreatedAt()).isNotNull();
    }
}

