package com.rubenzu03.rag_chatbot.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChatHistoryEncryptionServiceTest {

    @Test
    void shouldEncryptAndDecryptUsingSameConversationKey() {
        ConversationEncryptionKeyService keyService = mock(ConversationEncryptionKeyService.class);
        when(keyService.getOrCreateKey("userA::c1")).thenReturn(repeat((byte) 0x01));

        ChatHistoryEncryptionService encryptionService = new ChatHistoryEncryptionService(keyService);
        String plain = "hello secure world";

        String encrypted = encryptionService.encrypt(plain, "userA::c1");
        String decrypted = encryptionService.decrypt(encrypted, "userA::c1");

        assertNotEquals(plain, encrypted);
        assertEquals(plain, decrypted);
    }

    @Test
    void shouldNotDecryptWithDifferentConversationKey() {
        ConversationEncryptionKeyService keyService = mock(ConversationEncryptionKeyService.class);
        when(keyService.getOrCreateKey("userA::c1")).thenReturn(repeat((byte) 0x01));
        when(keyService.getOrCreateKey("userA::c2")).thenReturn(repeat((byte) 0x02));

        ChatHistoryEncryptionService encryptionService = new ChatHistoryEncryptionService(keyService);

        String encrypted = encryptionService.encrypt("secret", "userA::c1");
        String wrongDecryption = encryptionService.decrypt(encrypted, "userA::c2");

        assertEquals(encrypted, wrongDecryption);
    }

    private byte[] repeat(byte value) {
        byte[] key = new byte[32];
        for (int i = 0; i < key.length; i++) {
            key[i] = value;
        }
        return key;
    }
}

