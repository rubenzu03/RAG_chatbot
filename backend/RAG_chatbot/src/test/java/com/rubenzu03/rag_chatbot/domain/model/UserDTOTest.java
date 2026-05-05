package com.rubenzu03.rag_chatbot.domain.model;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;

class UserDTOTest {

    @Test
    void constructorWithoutArgs_initializesFieldsAsNull() {
        UserDTO user = new UserDTO();

        assertThat(user.getId()).isNull();
        assertThat(user.getEmail()).isNull();
        assertThat(user.getPassword()).isNull();
        assertThat(user.getCreatedAt()).isNull();
        assertThat(user.getLastLoginAt()).isNull();
    }

    @Test
    void constructorWithAllArgs_setsAllFields() {
        Timestamp createdAt = Timestamp.valueOf("2026-05-05 10:00:00");
        Timestamp lastLoginAt = Timestamp.valueOf("2026-05-05 11:00:00");

        UserDTO user = new UserDTO(1L, "user@mail.com", "secret", createdAt, lastLoginAt);

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getEmail()).isEqualTo("user@mail.com");
        assertThat(user.getPassword()).isEqualTo("secret");
        assertThat(user.getCreatedAt()).isEqualTo(createdAt);
        assertThat(user.getLastLoginAt()).isEqualTo(lastLoginAt);
    }

    @Test
    void constructorWithEmailAndPassword_setsTimestamps() {
        UserDTO user = new UserDTO("user@mail.com", "secret");

        assertThat(user.getEmail()).isEqualTo("user@mail.com");
        assertThat(user.getPassword()).isEqualTo("secret");
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getLastLoginAt()).isNotNull();
    }

    @Test
    void setters_updateAllFields() {
        UserDTO user = new UserDTO();
        Timestamp createdAt = Timestamp.valueOf("2026-05-05 12:00:00");
        Timestamp lastLoginAt = Timestamp.valueOf("2026-05-05 13:00:00");

        user.setId(2L);
        user.setEmail("another@mail.com");
        user.setPassword("new-secret");
        user.setCreatedAt(createdAt);
        user.setLastLoginAt(lastLoginAt);

        assertThat(user.getId()).isEqualTo(2L);
        assertThat(user.getEmail()).isEqualTo("another@mail.com");
        assertThat(user.getPassword()).isEqualTo("new-secret");
        assertThat(user.getCreatedAt()).isEqualTo(createdAt);
        assertThat(user.getLastLoginAt()).isEqualTo(lastLoginAt);
    }
}
