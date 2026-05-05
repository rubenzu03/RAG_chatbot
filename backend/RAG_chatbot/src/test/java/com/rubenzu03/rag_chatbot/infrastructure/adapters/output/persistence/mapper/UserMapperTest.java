package com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.mapper;

import com.rubenzu03.rag_chatbot.domain.model.UserDTO;
import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.entity.UserEntity;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper mapper = new UserMapper();

    @Test
    void toEntity_mapsAllFields() {
        UserDTO dto = new UserDTO();
        dto.setId(1L);
        dto.setEmail("user@example.com");
        dto.setPassword("secret");
        Timestamp created = new Timestamp(System.currentTimeMillis());
        Timestamp lastLogin = new Timestamp(System.currentTimeMillis());
        dto.setCreatedAt(created);
        dto.setLastLoginAt(lastLogin);

        UserEntity entity = mapper.toEntity(dto);

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getEmail()).isEqualTo("user@example.com");
        assertThat(entity.getPassword()).isEqualTo("secret");
        assertThat(entity.getCreatedAt()).isEqualTo(created);
        assertThat(entity.getLastLoginAt()).isEqualTo(lastLogin);
    }

    @Test
    void toDto_mapsAllFields() {
        UserEntity entity = new UserEntity();
        entity.setId(2L);
        entity.setEmail("user2@example.com");
        entity.setPassword("hash");
        Timestamp created = new Timestamp(System.currentTimeMillis());
        Timestamp lastLogin = new Timestamp(System.currentTimeMillis());
        entity.setCreatedAt(created);
        entity.setLastLoginAt(lastLogin);

        UserDTO dto = mapper.toDto(entity);

        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getEmail()).isEqualTo("user2@example.com");
        assertThat(dto.getPassword()).isEqualTo("hash");
        assertThat(dto.getCreatedAt()).isEqualTo(created);
        assertThat(dto.getLastLoginAt()).isEqualTo(lastLogin);
    }

    @Test
    void toEntity_returnsNullOnNullInput() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    void toDto_returnsNullOnNullInput() {
        assertThat(mapper.toDto(null)).isNull();
    }
}

