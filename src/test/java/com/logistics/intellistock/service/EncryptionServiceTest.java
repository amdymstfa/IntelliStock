package com.logistics.intellistock.service;

import com.logistics.intellistock.security.EncryptionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class EncryptionServiceTest {
    private EncryptionService encryption;

    @BeforeEach
    void setUp() {
        encryption = new EncryptionServiceImpl();
        ReflectionTestUtils.setField(encryption, "secretKey", "1234567890123456");
    }

    @Test
    @DisplayName("Should successfully encrypt and decrypt data")
    void shouldEncryptAndDecryptSuccessfully() throws Exception {
        String originalText = "Sensitive-Database-Value-123";

        String encryptedBase64 = encryption.encrypt(originalText);

        assertThat(encryptedBase64).isNotEqualTo(originalText);

        String decryptedText = encryption.decrypt(encryptedBase64);

        assertThat(decryptedText).isEqualTo(originalText);
    }

    @Test
    @DisplayName("Should produce different ciphertexts for the same input (Salt/IV Check)")
    void shouldProduceDifferentCiphertextsForSameInput() throws Exception {
        String data = "SameData";

        String encryptedBase64_1 = encryption.encrypt(data);
        String encryptedBase64_2 = encryption.encrypt(data);

        assertThat(encryptedBase64_1).isNotEqualTo(encryptedBase64_2);
    }

    @Test
    @DisplayName("Should throw exception when decryption fails with wrong key")
    void shouldThrowExceptionWithInvalidKey() throws Exception {
        String data = "sameData";
        String encryptedData = encryption.encrypt(data);

        ReflectionTestUtils.setField(encryption, "secretKey", "wrong-secret-key-123456");

        assertThatThrownBy(() -> encryption.decrypt(encryptedData))
                .isInstanceOf(Exception.class);

        ReflectionTestUtils.setField(encryption, "secretKey", "1234567890123456");
    }
}
