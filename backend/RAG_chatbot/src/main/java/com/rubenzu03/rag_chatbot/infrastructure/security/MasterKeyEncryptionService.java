package com.rubenzu03.rag_chatbot.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class MasterKeyEncryptionService {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String ALGORITHM = "AES";
    private static final int IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final byte[] masterKeyBytes;

    public MasterKeyEncryptionService(
            @Value("${app.security.master-key:default_32_bytes_master_key_for_dev_ONLY!}") String masterKey) {
        if (masterKey == null || masterKey.length() < 32) {
            throw new IllegalArgumentException("Master key must be at least 32 characters long");
        }
        this.masterKeyBytes = masterKey.substring(0, 32).getBytes(StandardCharsets.UTF_8);
    }

    public String wrapKey(byte[] rawKey) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            SECURE_RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(masterKeyBytes, ALGORITHM),
                    new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] encryptedKey = cipher.doFinal(rawKey);

            ByteBuffer payload = ByteBuffer.allocate(iv.length + encryptedKey.length);
            payload.put(iv);
            payload.put(encryptedKey);
            return Base64.getEncoder().encodeToString(payload.array());
        } catch (Exception e) {
            throw new RuntimeException("Error wrapping DEK with Master Key", e);
        }
    }

    public byte[] unwrapKey(String wrappedKeyBase64) {
        try {
            byte[] payload = Base64.getDecoder().decode(wrappedKeyBase64);
            if (payload.length <= IV_LENGTH) {
                throw new IllegalArgumentException("Invalid wrapped key payload");
            }

            ByteBuffer byteBuffer = ByteBuffer.wrap(payload);
            byte[] iv = new byte[IV_LENGTH];
            byteBuffer.get(iv);

            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(masterKeyBytes, ALGORITHM),
                    new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            return cipher.doFinal(cipherText);
        } catch (Exception e) {
            throw new RuntimeException("Error unwrapping DEK with Master Key", e);
        }
    }
}
