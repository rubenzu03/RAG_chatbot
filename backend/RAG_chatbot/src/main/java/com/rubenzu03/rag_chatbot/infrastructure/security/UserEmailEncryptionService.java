package com.rubenzu03.rag_chatbot.infrastructure.security;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class UserEmailEncryptionService {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String ALGORITHM = "AES";
    private static final int IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserEmailEncryptionKeyService userEmailEncryptionKeyService;

    public UserEmailEncryptionService(UserEmailEncryptionKeyService userEmailEncryptionKeyService) {
        this.userEmailEncryptionKeyService = userEmailEncryptionKeyService;
    }

    public String normalizeEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public String hashEmail(String email) {
        try {
            String normalized = normalizeEmail(email);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(normalized.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing user email", e);
        }
    }

    public String encryptEmail(String email) {
        try {
            String normalized = normalizeEmail(email);
            String emailHash = hashEmail(normalized);
            byte[] key = userEmailEncryptionKeyService.getOrCreateKey(emailHash);

            byte[] iv = new byte[IV_LENGTH];
            SECURE_RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, ALGORITHM),
                    new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] encryptedBytes = cipher.doFinal(normalized.getBytes(StandardCharsets.UTF_8));

            ByteBuffer payload = ByteBuffer.allocate(iv.length + encryptedBytes.length);
            payload.put(iv);
            payload.put(encryptedBytes);
            return Base64.getEncoder().encodeToString(payload.array());
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting user email", e);
        }
    }

    public String decryptEmail(String encryptedEmail, String emailHash) {
        try {
            if (encryptedEmail == null || encryptedEmail.isBlank()) {
                return encryptedEmail;
            }
            byte[] payload = Base64.getDecoder().decode(encryptedEmail);
            if (payload.length <= IV_LENGTH) {
                return encryptedEmail;
            }

            byte[] key = userEmailEncryptionKeyService.getOrCreateKey(emailHash);
            ByteBuffer byteBuffer = ByteBuffer.wrap(payload);
            byte[] iv = new byte[IV_LENGTH];
            byteBuffer.get(iv);

            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, ALGORITHM),
                    new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Email decryption failed. Data integrity may be compromised.", e);
        }
    }
}
