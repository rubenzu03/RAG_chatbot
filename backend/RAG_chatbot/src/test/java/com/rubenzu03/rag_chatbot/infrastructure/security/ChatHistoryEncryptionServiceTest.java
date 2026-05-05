package com.rubenzu03.rag_chatbot.infrastructure.security;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChatHistoryEncryptionServiceTest {

    @Test
    void encryptAndDecrypt_roundTrip() {
        ConversationEncryptionKeyService keyService = mock(ConversationEncryptionKeyService.class);
        when(keyService.getOrCreateKey("conv")).thenReturn(new byte[32]);
        ChatHistoryEncryptionService service = new ChatHistoryEncryptionService(keyService);

        String encrypted = service.encrypt("hello", "conv");
        String decrypted = service.decrypt(encrypted, "conv");

        assertThat(decrypted).isEqualTo("hello");
    }

    @Test
    void decrypt_returnsInputWhenPayloadTooShort() {
        ConversationEncryptionKeyService keyService = mock(ConversationEncryptionKeyService.class);
        when(keyService.getOrCreateKey("conv")).thenReturn(new byte[32]);
        ChatHistoryEncryptionService service = new ChatHistoryEncryptionService(keyService);

        String shortPayload = Base64.getEncoder().encodeToString(new byte[5]);

        assertThat(service.decrypt(shortPayload, "conv")).isEqualTo(shortPayload);
    }
}

