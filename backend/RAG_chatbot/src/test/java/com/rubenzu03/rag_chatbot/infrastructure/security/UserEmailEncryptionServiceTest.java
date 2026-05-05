package com.rubenzu03.rag_chatbot.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserEmailEncryptionServiceTest {

    private UserEmailEncryptionService userEmailEncryptionService;

    @BeforeEach
    void setUp() {
        UserEmailEncryptionKeyService keyService = mock(UserEmailEncryptionKeyService.class);
        when(keyService.getOrCreateKey(anyString())).thenReturn("0123456789abcdef0123456789abcdef".getBytes());
        userEmailEncryptionService = new UserEmailEncryptionService(keyService);
    }

    @Test
    void shouldHashEmailDeterministicallyAfterNormalization() {
        String hashA = userEmailEncryptionService.hashEmail("  User@Example.com ");
        String hashB = userEmailEncryptionService.hashEmail("user@example.com");

        assertNotNull(hashA);
        assertEquals(64, hashA.length());
        assertEquals(hashA, hashB);
    }

    @Test
    void shouldEncryptAndDecryptEmail() {
        String encrypted = userEmailEncryptionService.encryptEmail("User@Example.com");
        String hash = userEmailEncryptionService.hashEmail("user@example.com");

        String decrypted = userEmailEncryptionService.decryptEmail(encrypted, hash);

        assertEquals("user@example.com", decrypted);
    }
}

