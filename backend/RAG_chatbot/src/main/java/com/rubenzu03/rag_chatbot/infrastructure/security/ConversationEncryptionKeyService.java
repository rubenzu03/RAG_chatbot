package com.rubenzu03.rag_chatbot.infrastructure.security;

import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.ChatEncryptionKeyRepository;
import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.entity.ChatEncryptionKeyEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;

@Service
public class ConversationEncryptionKeyService {

    private static final int KEY_BYTES = 32;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final ChatEncryptionKeyRepository chatEncryptionKeyRepository;
    private final MasterKeyEncryptionService masterKeyEncryptionService;

    public ConversationEncryptionKeyService(ChatEncryptionKeyRepository chatEncryptionKeyRepository,
            MasterKeyEncryptionService masterKeyEncryptionService) {
        this.chatEncryptionKeyRepository = chatEncryptionKeyRepository;
        this.masterKeyEncryptionService = masterKeyEncryptionService;
    }

    @Transactional
    public byte[] getOrCreateKey(String conversationId) {
        String safeConversationId = (conversationId == null || conversationId.isBlank())
                ? "anonymous::default"
                : conversationId.trim();

        return chatEncryptionKeyRepository.findById(safeConversationId)
                .map(entity -> masterKeyEncryptionService.unwrapKey(entity.getKeyMaterial()))
                .orElseGet(() -> createAndPersist(safeConversationId));
    }

    private byte[] createAndPersist(String conversationId) {
        byte[] rawKey = new byte[KEY_BYTES];
        SECURE_RANDOM.nextBytes(rawKey);

        ChatEncryptionKeyEntity entity = new ChatEncryptionKeyEntity();
        entity.setConversationId(conversationId);
        entity.setKeyMaterial(masterKeyEncryptionService.wrapKey(rawKey));
        entity.setCreatedAt(Timestamp.from(Instant.now()));
        chatEncryptionKeyRepository.save(entity);

        return rawKey;
    }
}
