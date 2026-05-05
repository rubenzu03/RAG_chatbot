package com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.entity;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;

class UserEmailEncryptionKeyEntityTest {

    @Test
    void testUserEmailEncryptionKeyEntityCreation() {
        UserEmailEncryptionKeyEntity entity = new UserEmailEncryptionKeyEntity();
        entity.setEmailHash("email-hash-123");
        entity.setKeyMaterial("key-value");
        entity.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        assertThat(entity.getEmailHash()).isEqualTo("email-hash-123");
        assertThat(entity.getKeyMaterial()).isEqualTo("key-value");
        assertThat(entity.getCreatedAt()).isNotNull();
    }

    @Test
    void testUserEmailEncryptionKeySetters() {
        UserEmailEncryptionKeyEntity entity = new UserEmailEncryptionKeyEntity();

        entity.setEmailHash("user-456");
        entity.setKeyMaterial("derived-key");

        assertThat(entity.getEmailHash()).isEqualTo("user-456");
        assertThat(entity.getKeyMaterial()).isEqualTo("derived-key");
    }

    @Test
    void testUserEmailEncryptionKeyCreatedAtTimestamp() {
        UserEmailEncryptionKeyEntity entity = new UserEmailEncryptionKeyEntity();
        Timestamp now = new Timestamp(System.currentTimeMillis());

        entity.setCreatedAt(now);
        assertThat(entity.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void testUserEmailEncryptionKeyWithAllFields() {
        UserEmailEncryptionKeyEntity entity = new UserEmailEncryptionKeyEntity();
        Timestamp now = new Timestamp(System.currentTimeMillis());

        entity.setEmailHash("complete-hash");
        entity.setKeyMaterial("complete-key");
        entity.setCreatedAt(now);

        assertThat(entity.getEmailHash()).isEqualTo("complete-hash");
        assertThat(entity.getKeyMaterial()).isEqualTo("complete-key");
        assertThat(entity.getCreatedAt()).isNotNull();
    }

    @Test
    void testUserEmailEncryptionKeyMultipleInstances() {
        UserEmailEncryptionKeyEntity entity1 = new UserEmailEncryptionKeyEntity();
        entity1.setEmailHash("hash1");
        entity1.setKeyMaterial("key1");

        UserEmailEncryptionKeyEntity entity2 = new UserEmailEncryptionKeyEntity();
        entity2.setEmailHash("hash2");
        entity2.setKeyMaterial("key2");

        assertThat(entity1.getEmailHash()).isNotEqualTo(entity2.getEmailHash());
        assertThat(entity1.getKeyMaterial()).isNotEqualTo(entity2.getKeyMaterial());
    }
}

