package com.signongroup.focus.service;

import com.github.javakeyring.BackendNotSupportedException;
import com.github.javakeyring.Keyring;
import com.github.javakeyring.PasswordAccessException;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.prefs.Preferences;

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

    private static final String SERVICE_NAME = "FocusJiraApp";

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
     *
     * @param key Logical key (e.g. "jira_token")
     * @return The secret, or {@code null} if not found
     */
    public String getSecret(String key) {
        if (keyring == null) {
            log.error("Keyring not available – cannot read secret for key: {}", key);
            return null;
        }

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
}
