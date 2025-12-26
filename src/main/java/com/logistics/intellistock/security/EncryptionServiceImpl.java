package com.logistics.intellistock.security;

import com.logistics.intellistock.service.EncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Service
public class EncryptionServiceImpl implements EncryptionService {

    @Value("${encryption.secret-key}")
    private String secretKey;

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    @Override
    public String encrypt(String data) throws Exception {
        try {
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            byte[] cipherText = cipher.doFinal(data.getBytes());

            byte[] combined = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("Error encrypting data: {}", e.getMessage());
            throw new Exception("Encryption failed", e);
        }
    }

    @Override
    public String decrypt(String encryptedData) throws Exception {
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedData);

            byte[] iv = new byte[16];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            int cipherTextLen = combined.length - iv.length;
            byte[] cipherText = new byte[cipherTextLen];
            System.arraycopy(combined, iv.length, cipherText, 0, cipherTextLen);

            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            return new String(cipher.doFinal(cipherText));
        } catch (Exception e) {
            log.error("Error decrypting data: {}", e.getMessage());
            throw new Exception("Decryption failed", e);
        }
    }
}
