package com.docheck.flow.application.service.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Base64;

@Service
public class EncryptionService {

    @Value("${app.security.ai.encryption.password}")
    private String password;

    @Value("${app.security.ai.encryption.salt}")
    private String salt;

    private static final String ALGORITHM = "AES";
    private static final String LEGACY_PREFIX = "encrypted(";

    private SecretKey getSecretKey() throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(StandardCharsets.UTF_8), 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), ALGORITHM);
    }

    public String encrypt(String data) {
        if (data == null) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criptografar os dados", e);
        }
    }

    public String decrypt(String encryptedData) {
        if (encryptedData == null) {
            return null;
        }

        // Handle legacy placeholder encryption: "encrypted(...)"
        if (encryptedData.startsWith(LEGACY_PREFIX) && encryptedData.endsWith(")")) {
            return encryptedData.substring(LEGACY_PREFIX.length(), encryptedData.length() - 1);
        }

        // Attempt to decrypt using the new AES encryption method
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey());
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // If decryption fails, it's likely the data is already in plaintext.
            // This ensures a smooth transition for unencrypted keys.
            return encryptedData;
        }
    }
}
