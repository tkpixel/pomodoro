package com.signongroup.pomodoro.service;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

@Singleton
public class JiraAuthService {

    private static final String JIRA_URL_KEY = "jira_url";
    private static final String JIRA_EMAIL_KEY = "jira_email";
    private static final String JIRA_TOKEN_KEY = "jira_token";

    private final SecretManager secretManager;
    private final HttpClient httpClient;

    @Inject
    public JiraAuthService(SecretManager secretManager) {
        this.secretManager = secretManager;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public CompletableFuture<Boolean> testConnection(String url, String email, String token) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String cleanUrl = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
                URI uri = URI.create(cleanUrl + "/rest/api/3/myself");

                String auth = email + ":" + token;
                String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
                String authHeader = "Basic " + encodedAuth;

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(uri)
                        .header("Authorization", authHeader)
                        .header("Accept", "application/json")
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                boolean success = response.statusCode() == 200;
                if (success) {
                    saveCredentials(cleanUrl, email, token);
                }
                return success;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    private void saveCredentials(String url, String email, String token) {
        secretManager.savePlaintext(JIRA_URL_KEY, url);
        secretManager.savePlaintext(JIRA_EMAIL_KEY, email);
        secretManager.saveSecret(JIRA_TOKEN_KEY, token);
    }

    public String getSavedUrl() {
        return secretManager.getPlaintext(JIRA_URL_KEY);
    }

    public String getSavedEmail() {
        return secretManager.getPlaintext(JIRA_EMAIL_KEY);
    }

    public String getSavedToken() {
        return secretManager.getSecret(JIRA_TOKEN_KEY);
    }
}
