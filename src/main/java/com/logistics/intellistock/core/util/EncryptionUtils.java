package com.logistics.intellistock.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class EncryptionUtils {

  private static final String ALGORITHM = "AES";

  public static String encrypt(String data, String secretKey) throws Exception {
    SecretKeySpec keySpec = new SecretKeySpec(
      padKey(secretKey).getBytes(StandardCharsets.UTF_8),
      ALGORITHM
    );
    Cipher cipher = Cipher.getInstance(ALGORITHM);
    cipher.init(Cipher.ENCRYPT_MODE, keySpec);
    byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
    return Base64.getEncoder().encodeToString(encrypted);
  }

  public static String decrypt(String encryptedData, String secretKey) throws Exception {
    SecretKeySpec keySpec = new SecretKeySpec(
      padKey(secretKey).getBytes(StandardCharsets.UTF_8),
      ALGORITHM
    );
    Cipher cipher = Cipher.getInstance(ALGORITHM);
    cipher.init(Cipher.DECRYPT_MODE, keySpec);
    byte[] decoded = Base64.getDecoder().decode(encryptedData);
    byte[] decrypted = cipher.doFinal(decoded);
    return new String(decrypted, StandardCharsets.UTF_8);
  }

  private static String padKey(String key) {
    // Ensure key is exactly 16 bytes (128 bits) for AES
    if (key.length() < 16) {
      return String.format("%-16s", key).replace(' ', '0');
    } else if (key.length() > 16) {
      return key.substring(0, 16);
    }
    return key;
  }

  private EncryptionUtils() {
    throw new IllegalStateException("Utility class");
  }
}
