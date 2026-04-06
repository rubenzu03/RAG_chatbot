package com.rubenzu03.rag_chatbot.infrastructure.security;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class ChatHistoryEncryptionService {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String ALGORITHM = "AES";
    private static final int IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final ConversationEncryptionKeyService conversationEncryptionKeyService;

    public ChatHistoryEncryptionService(ConversationEncryptionKeyService conversationEncryptionKeyService) {
        this.conversationEncryptionKeyService = conversationEncryptionKeyService;
    }

    public String encrypt(String data, String conversationId) {
        try {
            byte[] key = conversationEncryptionKeyService.getOrCreateKey(conversationId);
            byte[] iv = new byte[IV_LENGTH];
            SECURE_RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, ALGORITHM), new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

            ByteBuffer payload = ByteBuffer.allocate(iv.length + encryptedBytes.length);
            payload.put(iv);
            payload.put(encryptedBytes);
            return Base64.getEncoder().encodeToString(payload.array());
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting chat history", e);
        }
    }

    public String decrypt(String encryptedData, String conversationId) {
        try {
            byte[] key = conversationEncryptionKeyService.getOrCreateKey(conversationId);
            byte[] payload = Base64.getDecoder().decode(encryptedData);

            if (payload.length <= IV_LENGTH) {
                return encryptedData;
            }

            ByteBuffer byteBuffer = ByteBuffer.wrap(payload);
            byte[] iv = new byte[IV_LENGTH];
            byteBuffer.get(iv);

            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, ALGORITHM), new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return encryptedData;
        }
    }
}
