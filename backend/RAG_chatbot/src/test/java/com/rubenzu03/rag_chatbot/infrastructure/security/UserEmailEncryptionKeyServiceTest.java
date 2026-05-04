package com.rubenzu03.rag_chatbot.infrastructure.security;

import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.UserEmailEncryptionKeyRepository;
import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.entity.UserEmailEncryptionKeyEntity;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserEmailEncryptionKeyServiceTest {

    @Test
    void getOrCreateKey_returnsExistingKey() {
        UserEmailEncryptionKeyRepository repository = mock(UserEmailEncryptionKeyRepository.class);
        MasterKeyEncryptionService masterKey = mock(MasterKeyEncryptionService.class);
        UserEmailEncryptionKeyService service = new UserEmailEncryptionKeyService(repository, masterKey);

        UserEmailEncryptionKeyEntity entity = new UserEmailEncryptionKeyEntity();
        entity.setEmailHash("hash");
        entity.setKeyMaterial("wrapped");
        when(repository.findById("hash")).thenReturn(Optional.of(entity));
        when(masterKey.unwrapKey("wrapped")).thenReturn(new byte[]{1, 2, 3});

        byte[] key = service.getOrCreateKey("hash");

        assertThat(key).containsExactly(1, 2, 3);
        verify(repository, never()).save(any(UserEmailEncryptionKeyEntity.class));
    }

    @Test
    void getOrCreateKey_createsAndSavesKey() {
        UserEmailEncryptionKeyRepository repository = mock(UserEmailEncryptionKeyRepository.class);
        MasterKeyEncryptionService masterKey = mock(MasterKeyEncryptionService.class);
        UserEmailEncryptionKeyService service = new UserEmailEncryptionKeyService(repository, masterKey);
        when(repository.findById("hash")).thenReturn(Optional.empty());
        when(masterKey.wrapKey(any(byte[].class))).thenReturn("wrapped");

        byte[] key = service.getOrCreateKey("hash");

        assertThat(key).hasSize(32);
        ArgumentCaptor<UserEmailEncryptionKeyEntity> captor = ArgumentCaptor.forClass(UserEmailEncryptionKeyEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getEmailHash()).isEqualTo("hash");
        assertThat(captor.getValue().getKeyMaterial()).isEqualTo("wrapped");
    }
}

