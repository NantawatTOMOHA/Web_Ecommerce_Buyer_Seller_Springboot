package com.shop.backend.util;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.*;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class CryptoUtil {

    @Value("${app.encryption.key}")
    private String encryptionKey;

    private byte[] keyBytes;

    private static final int IV_LENGTH = 16;
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    @PostConstruct
    public void init() {
        keyBytes = encryptionKey.getBytes();
    }

    public byte[] encrypt(byte[] data) throws Exception {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        byte[] encrypted = cipher.doFinal(data);

        byte[] encryptedWithIv = new byte[IV_LENGTH + encrypted.length];
        System.arraycopy(iv, 0, encryptedWithIv, 0, IV_LENGTH);
        System.arraycopy(encrypted, 0, encryptedWithIv, IV_LENGTH, encrypted.length);
        return encryptedWithIv;
    }

    public byte[] decrypt(byte[] encrypted) throws Exception {
        byte[] iv = new byte[IV_LENGTH];
        System.arraycopy(encrypted, 0, iv, 0, IV_LENGTH);

        byte[] encryptedData = new byte[encrypted.length - IV_LENGTH];
        System.arraycopy(encrypted, IV_LENGTH, encryptedData, 0, encryptedData.length);

        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        return cipher.doFinal(encryptedData);
    }

    public boolean isValidBase64Image(String base64String) {
        return base64String != null && base64String.matches("^data:image/(jpeg|png);base64,[A-Za-z0-9+/=]+$");
    }

    public byte[] parseBase64Image(String base64String) {
        String base64Data = base64String.split(",")[1];
        return Base64.getDecoder().decode(base64Data);
    }

    public String toBase64Image(byte[] data) {
        String base64 = Base64.getEncoder().encodeToString(data);
        return "data:image/jpeg;base64," + base64;
    }
}
