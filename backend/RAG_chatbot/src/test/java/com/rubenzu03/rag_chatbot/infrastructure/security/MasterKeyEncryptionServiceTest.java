package com.rubenzu03.rag_chatbot.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MasterKeyEncryptionServiceTest {

    @Test
    void wrapAndUnwrapKey_roundTrip() {
        MasterKeyEncryptionService service = new MasterKeyEncryptionService("0123456789abcdef0123456789abcdef");
        byte[] rawKey = new byte[32];
        for (int i = 0; i < rawKey.length; i++) {
            rawKey[i] = (byte) i;
        }

        String wrapped = service.wrapKey(rawKey);
        byte[] unwrapped = service.unwrapKey(wrapped);

        assertThat(unwrapped).isEqualTo(rawKey);
    }

    @Test
    void constructor_rejectsShortKey() {
        assertThatThrownBy(() -> new MasterKeyEncryptionService("short"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Master key must be at least 32 characters long");
    }
}

