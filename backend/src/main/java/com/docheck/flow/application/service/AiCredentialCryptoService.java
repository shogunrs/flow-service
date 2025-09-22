package com.docheck.flow.application.service;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class AiCredentialCryptoService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";

    // Em produção, isso deveria vir de variáveis de ambiente ou configuração segura
    private static final String SECRET_KEY = "MySecretKey12345"; // 16 bytes for AES-128

    public String encrypt(String plainText) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting data", e);
        }
    }

    public String decrypt(String encryptedText) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting data", e);
        }
    }

    public String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return "****";
        }

        String prefix = apiKey.substring(0, Math.min(4, apiKey.length()));
        String suffix = apiKey.substring(Math.max(0, apiKey.length() - 4));
        int maskedLength = apiKey.length() - 8;
        String masked = "*".repeat(Math.max(4, maskedLength));

        return prefix + masked + suffix;
    }

    public String getKeyPreview(String apiKey) {
        if (apiKey == null || apiKey.length() < 4) {
            return "****";
        }
        return apiKey.substring(0, Math.min(8, apiKey.length())) + "...";
    }
}