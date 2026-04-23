package com.signongroup.pomodoro.service;

import com.github.javakeyring.BackendNotSupportedException;
import com.github.javakeyring.Keyring;
import com.github.javakeyring.PasswordAccessException;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.prefs.Preferences;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Manages secure storage of secrets using the native OS Credential Manager
 * (Windows Credential Manager on Windows) via java-keyring.
 *
 * Non-sensitive plaintext values (e.g. URLs, email addresses) continue to use
 * java.util.prefs.Preferences for simplicity.
 */
@Singleton
public class SecretManager {

    private static final Logger log = LoggerFactory.getLogger(SecretManager.class);

    private static final String SERVICE_NAME = "PomodoroJiraApp";

    // ── Legacy migration constants (used ONLY during one-time migration, then removed) ──
    // These are kept temporarily to allow decryption of the old format during migration.
    // They will be deleted in a follow-up cleanup commit once all users have migrated.
    private static final String LEGACY_ALGORITHM = "AES";
    @SuppressWarnings("java:S2068") // intentional: this is the OLD hardcoded salt being migrated away from
    private static final String LEGACY_SALT = "M0n0l1thS4lt";

    private final Keyring keyring;

    /**
     * Constructs a SecretManager with Keyring.
     */
    public SecretManager() {
        Keyring k;
        try {
            k = Keyring.create();
        } catch (BackendNotSupportedException e) {
            log.warn("Native Keyring backend not available, falling back to in-memory stub. Secrets will NOT persist.", e);
            k = null;
        }
        this.keyring = k;
    }

    // ── Public API ────────────────────────────────────────────────────────────────────────

    /**
     * Saves a sensitive secret to the OS Credential Manager.
     *
     * @param key   Logical key (e.g. "jira_token")
     * @param secretValue The plaintext secret value to store
     */
    public void saveSecret(String key, String secretValue) {
        if (keyring == null) {
            log.error("Keyring not available – cannot save secret for key: {}", key);
            return;
        }
        try {
            keyring.setPassword(SERVICE_NAME, key, secretValue);
        } catch (PasswordAccessException e) {
            throw new RuntimeException("Failed to save secret to Windows Credential Manager for key: " + key, e);
        }
    }

    /**
     * Retrieves a sensitive secret from the OS Credential Manager.
     * Automatically migrates any legacy encrypted value from Preferences on first access.
     *
     * @param key Logical key (e.g. "jira_token")
     * @return The decrypted secret, or {@code null} if not found
     */
    public String getSecret(String key) {
        if (keyring == null) {
            log.error("Keyring not available – cannot read secret for key: {}", key);
            return null;
        }

        // One-time migration: if the old encrypted value exists in Preferences, migrate it now
        migrateIfNeeded(key);

        try {
            return keyring.getPassword(SERVICE_NAME, key);
        } catch (PasswordAccessException e) {
            log.debug("Secret not found in Credential Manager for key: {}", key);
            return null;
        }
    }

    /**
     * Saves a non-sensitive configuration value using java.util.prefs.Preferences.
     *
     * @param key   Logical key
     * @param value The value
     */
    public void savePlaintext(String key, String value) {
        Preferences prefs = Preferences.userNodeForPackage(SecretManager.class);
        prefs.put(key, value);
    }

    /**
     * Retrieves a non-sensitive configuration value from java.util.prefs.Preferences.
     *
     * @param key Logical key
     * @return The retrieved value
     */
    public String getPlaintext(String key) {
        Preferences prefs = Preferences.userNodeForPackage(SecretManager.class);
        return prefs.get(key, "");
    }

    // ── Legacy Migration ──────────────────────────────────────────────────────────────────

    /**
     * Checks whether an old AES-encrypted value exists in Preferences for the given key.
     * If found: decrypts it with the legacy algorithm, saves it to the Credential Manager,
     * and removes the old entry from Preferences.
     *
     * This method is idempotent – it is safe to call multiple times.
     * It will be removed in a future cleanup once migration is confirmed complete.
     */
    @SuppressWarnings("checkstyle:IllegalCatch")
    private void migrateIfNeeded(String key) {
        Preferences prefs = Preferences.userNodeForPackage(SecretManager.class);
        String legacyEncrypted = prefs.get(key, null);
        if (legacyEncrypted == null) {
            return; // Nothing to migrate
        }
        log.info("Found legacy encrypted secret for key '{}' in Preferences. Migrating to Windows Credential Manager...", key);
        try {
            String decrypted = legacyDecrypt(legacyEncrypted);
            if (decrypted != null) {
                saveSecret(key, decrypted);
                prefs.remove(key);
                log.info("Migration successful for key '{}'. Old entry removed from Preferences.", key);
            }
        } catch (Exception e) {
            log.warn("Could not migrate legacy secret for key '{}'. User may need to re-enter credentials.", key, e);
            // Do NOT delete the old entry on failure – leave it for manual recovery
        }
    }

    /**
     * @deprecated Used only for one-time migration of the old AES/ECB encrypted format.
     *             Will be deleted after migration cleanup.
     * @param encryptedValue the value
     * @return the decrypted value
     * @throws Exception if something goes wrong
     */
    @Deprecated(since = "post-migration", forRemoval = true)
    @SuppressWarnings("checkstyle:IllegalCatch")
    private String legacyDecrypt(String encryptedValue) throws Exception {
        if (encryptedValue == null) {
            return null;
        }
        String username = System.getProperty("user.name", "defaultUser");
        String keyString = username + LEGACY_SALT;
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = sha.digest(keyString.getBytes(StandardCharsets.UTF_8));
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance(LEGACY_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedValue));
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}
