package com.rubenzu03.rag_chatbot.infrastructure.security;

import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.ChatEncryptionKeyRepository;
import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.entity.ChatEncryptionKeyEntity;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConversationEncryptionKeyServiceTest {

    @Test
    void getOrCreateKey_returnsExistingKey() {
        ChatEncryptionKeyRepository repository = mock(ChatEncryptionKeyRepository.class);
        MasterKeyEncryptionService masterKey = mock(MasterKeyEncryptionService.class);
        ConversationEncryptionKeyService service = new ConversationEncryptionKeyService(repository, masterKey);

        ChatEncryptionKeyEntity entity = new ChatEncryptionKeyEntity();
        entity.setConversationId("conv");
        entity.setKeyMaterial("wrapped");
        when(repository.findById("conv")).thenReturn(Optional.of(entity));
        when(masterKey.unwrapKey("wrapped")).thenReturn(new byte[]{4, 5});

        byte[] key = service.getOrCreateKey("conv");

        assertThat(key).containsExactly(4, 5);
        verify(repository, never()).save(any(ChatEncryptionKeyEntity.class));
    }

    @Test
    void getOrCreateKey_createsKeyForBlankConversation() {
        ChatEncryptionKeyRepository repository = mock(ChatEncryptionKeyRepository.class);
        MasterKeyEncryptionService masterKey = mock(MasterKeyEncryptionService.class);
        ConversationEncryptionKeyService service = new ConversationEncryptionKeyService(repository, masterKey);
        when(repository.findById("anonymous::default")).thenReturn(Optional.empty());
        when(masterKey.wrapKey(any(byte[].class))).thenReturn("wrapped");

        byte[] key = service.getOrCreateKey(" ");

        assertThat(key).hasSize(32);
        ArgumentCaptor<ChatEncryptionKeyEntity> captor = ArgumentCaptor.forClass(ChatEncryptionKeyEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getConversationId()).isEqualTo("anonymous::default");
        assertThat(captor.getValue().getKeyMaterial()).isEqualTo("wrapped");
    }
}

