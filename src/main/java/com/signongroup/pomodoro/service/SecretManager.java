package com.signongroup.pomodoro.service;

import jakarta.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.prefs.Preferences;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

@Singleton
public class SecretManager {

    private static final String ALGORITHM = "AES";
    private static final String SALT = "M0n0l1thS4lt";

    public void saveSecret(String key, String secretValue) {
        try {
            String encrypted = encrypt(secretValue);
            Preferences prefs = Preferences.userNodeForPackage(SecretManager.class);
            prefs.put(key, encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save secret", e);
        }
    }

    public String getSecret(String key) {
        Preferences prefs = Preferences.userNodeForPackage(SecretManager.class);
        String encrypted = prefs.get(key, null);
        if (encrypted == null) {
            return null;
        }
        try {
            return decrypt(encrypted);
        } catch (Exception e) {
            System.err.println("Failed to decrypt secret for key: " + key);
            return null;
        }
    }

    public void savePlaintext(String key, String value) {
        Preferences prefs = Preferences.userNodeForPackage(SecretManager.class);
        prefs.put(key, value);
    }

    public String getPlaintext(String key) {
        Preferences prefs = Preferences.userNodeForPackage(SecretManager.class);
        return prefs.get(key, "");
    }

    private SecretKeySpec generateKey() throws NoSuchAlgorithmException {
        String username = System.getProperty("user.name", "defaultUser");
        String keyString = username + SALT;
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = sha.digest(keyString.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(keyBytes, "AES");
    }

    private String encrypt(String value) throws Exception {
        if (value == null) return null;
        SecretKeySpec secretKey = generateKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    private String decrypt(String encryptedValue) throws Exception {
        if (encryptedValue == null) return null;
        SecretKeySpec secretKey = generateKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedValue));
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}
