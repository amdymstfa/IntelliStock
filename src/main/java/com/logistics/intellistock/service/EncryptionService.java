package com.logistics.intellistock.service;

public interface EncryptionService {
    public String encrypt(String data) throws Exception;
    public String decrypt(String encryptedData) throws Exception;
}
