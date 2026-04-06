package com.rubenzu03.rag_chatbot.infrastructure.security;

import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.UserEmailEncryptionKeyRepository;
import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.entity.UserEmailEncryptionKeyEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;

@Service
public class UserEmailEncryptionKeyService {

    private static final int KEY_BYTES = 32;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserEmailEncryptionKeyRepository userEmailEncryptionKeyRepository;

    public UserEmailEncryptionKeyService(UserEmailEncryptionKeyRepository userEmailEncryptionKeyRepository) {
        this.userEmailEncryptionKeyRepository = userEmailEncryptionKeyRepository;
    }

    @Transactional
    public byte[] getOrCreateKey(String emailHash) {
        return userEmailEncryptionKeyRepository.findById(emailHash)
                .map(entity -> Base64.getDecoder().decode(entity.getKeyMaterial()))
                .orElseGet(() -> createAndPersist(emailHash));
    }

    private byte[] createAndPersist(String emailHash) {
        byte[] rawKey = new byte[KEY_BYTES];
        SECURE_RANDOM.nextBytes(rawKey);

        UserEmailEncryptionKeyEntity entity = new UserEmailEncryptionKeyEntity();
        entity.setEmailHash(emailHash);
        entity.setKeyMaterial(Base64.getEncoder().encodeToString(rawKey));
        entity.setCreatedAt(Timestamp.from(Instant.now()));
        userEmailEncryptionKeyRepository.save(entity);

        return rawKey;
    }
}

