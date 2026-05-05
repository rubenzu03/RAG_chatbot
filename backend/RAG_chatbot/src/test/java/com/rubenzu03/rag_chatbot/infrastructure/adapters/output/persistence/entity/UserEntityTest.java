package com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.entity;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;

class UserEntityTest {

    @Test
    void testUserEntityCreation() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setEmailHash("hash123");
        user.setPassword("password123");
        user.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getEmailHash()).isEqualTo("hash123");
        assertThat(user.getPassword()).isEqualTo("password123");
        assertThat(user.getCreatedAt()).isNotNull();
    }

    @Test
    void testUserEntitySetters() {
        UserEntity user = new UserEntity();

        user.setEmail("user@test.com");
        user.setPassword("securePass");

        assertThat(user.getEmail()).isEqualTo("user@test.com");
        assertThat(user.getPassword()).isEqualTo("securePass");
    }

    @Test
    void testUserEntityWithId() {
        UserEntity user = new UserEntity();
        user.setId(42L);

        assertThat(user.getId()).isEqualTo(42L);
    }

    @Test
    void testUserEntityLastLoginAt() {
        UserEntity user = new UserEntity();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        user.setLastLoginAt(timestamp);
        assertThat(user.getLastLoginAt()).isEqualTo(timestamp);
    }

    @Test
    void testUserEntityWithAllFields() {
        UserEntity user = new UserEntity();
        Timestamp now = new Timestamp(System.currentTimeMillis());

        user.setId(100L);
        user.setEmail("complete@test.com");
        user.setEmailHash("fullhash");
        user.setPassword("fullpass");
        user.setCreatedAt(now);
        user.setLastLoginAt(now);

        assertThat(user.getId()).isEqualTo(100L);
        assertThat(user.getEmail()).isEqualTo("complete@test.com");
        assertThat(user.getEmailHash()).isEqualTo("fullhash");
        assertThat(user.getPassword()).isEqualTo("fullpass");
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getLastLoginAt()).isNotNull();
    }
}
