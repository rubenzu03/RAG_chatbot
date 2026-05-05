package com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.adapter;

import com.rubenzu03.rag_chatbot.domain.model.UserDTO;
import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.DataUserRepository;
import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.entity.UserEntity;
import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.mapper.UserMapper;
import com.rubenzu03.rag_chatbot.infrastructure.security.UserEmailEncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserPersistenceAdapterTest {

    private DataUserRepository dataUserRepository;
    private UserMapper userMapper;
    private UserEmailEncryptionService userEmailEncryptionService;
    private UserPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        dataUserRepository = mock(DataUserRepository.class);
        userMapper = mock(UserMapper.class);
        userEmailEncryptionService = mock(UserEmailEncryptionService.class);
        adapter = new UserPersistenceAdapter(dataUserRepository, userMapper, userEmailEncryptionService);
    }

    @Test
    void shouldFindEncryptedUserByEmailHash() {
        UserEntity entity = new UserEntity();
        entity.setEmail("encrypted-email");
        entity.setEmailHash("hash");

        UserDTO dto = new UserDTO();
        dto.setEmail("encrypted-email");

        when(userEmailEncryptionService.normalizeEmail("User@Example.com")).thenReturn("user@example.com");
        when(userEmailEncryptionService.hashEmail("user@example.com")).thenReturn("hash");
        when(dataUserRepository.findByEmailHash("hash")).thenReturn(Optional.of(entity));
        when(userMapper.toDto(entity)).thenReturn(dto);
        when(userEmailEncryptionService.decryptEmail("encrypted-email", "hash")).thenReturn("user@example.com");

        Optional<UserDTO> result = adapter.findByEmail("User@Example.com");

        assertTrue(result.isPresent());
        assertEquals("user@example.com", result.get().getEmail());
    }

    @Test
    void shouldReturnEmptyWhenEmailHashDoesNotExist() {
        when(userEmailEncryptionService.normalizeEmail("User@Example.com")).thenReturn("user@example.com");
        when(userEmailEncryptionService.hashEmail("user@example.com")).thenReturn("hash");
        when(dataUserRepository.findByEmailHash("hash")).thenReturn(Optional.empty());

        Optional<UserDTO> result = adapter.findByEmail("User@Example.com");

        assertFalse(result.isPresent());
    }
}



