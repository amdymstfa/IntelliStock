package com.logistics.intellistock.security;

import com.logistics.intellistock.service.EncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Service
public class EncryptionServiceImpl implements EncryptionService {

    @Value("${encryption.secret-key}")
    private String secretKey;

    @Value("${encryption.algorithm:AES}")
    private String algorithm;

    @Override
    public String encrypt(String data) throws Exception {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(
                    padKey(secretKey).getBytes(StandardCharsets.UTF_8),
                    algorithm
            );
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("Error encrypting data: {}", e.getMessage());
            throw new Exception("Encryption failed", e);
        }
    }

    @Override
    public String decrypt(String encryptedData) throws Exception {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(
                    padKey(secretKey).getBytes(StandardCharsets.UTF_8),
                    algorithm
            );
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decoded = Base64.getDecoder().decode(encryptedData);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Error decrypting data: {}", e.getMessage());
            throw new Exception("Decryption failed", e);
        }
    }

    private String padKey(String key) {
        if (key.length() < 16) {
            return String.format("%-16s", key).replace(' ', '0');
        } else if (key.length() > 16) {
            return key.substring(0, 16);
        }
        return key;
    }
}
