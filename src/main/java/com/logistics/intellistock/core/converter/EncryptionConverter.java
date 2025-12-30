package com.logistics.intellistock.core.converter;

import com.logistics.intellistock.service.EncryptionService;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Converter
@Component
public class EncryptionConverter implements AttributeConverter<BigDecimal, String> {

    private static EncryptionService encryptionService;

    @Autowired
    public void setEncryptionService(@Lazy EncryptionService service) {
        EncryptionConverter.encryptionService = service;
    }
    @Override
    public String convertToDatabaseColumn(BigDecimal attribute) {
        if (attribute == null) return null;
        try {
            return encryptionService.encrypt(attribute.toString());
        } catch (Exception e) {
            log.error("failed to encrypt converted attribute {} :", e.getMessage());
            throw new RuntimeException("");
        }
    }

    @Override
    public BigDecimal convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            String decrypted = encryptionService.decrypt(dbData);
            return new BigDecimal(decrypted);
        } catch (Exception e) {
            log.error("failed to decrypt converted data {} :", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
