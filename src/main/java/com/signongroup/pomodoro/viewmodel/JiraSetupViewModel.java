package com.signongroup.pomodoro.viewmodel;

import com.signongroup.pomodoro.service.JiraAuthService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

@Singleton
public class JiraSetupViewModel {

    private final StringProperty url = new SimpleStringProperty("");
    private final StringProperty email = new SimpleStringProperty("");
    private final StringProperty token = new SimpleStringProperty("");
    private final BooleanProperty isConnecting = new SimpleBooleanProperty(false);
    private final StringProperty statusMessage = new SimpleStringProperty("");
    private final BooleanProperty isSuccess = new SimpleBooleanProperty(false);

    private final JiraAuthService jiraAuthService;

    @Inject
    public JiraSetupViewModel(JiraAuthService jiraAuthService) {
        this.jiraAuthService = jiraAuthService;
        loadSavedCredentials();
    }

    private void loadSavedCredentials() {
        String savedUrl = jiraAuthService.getSavedUrl();
        if (savedUrl != null && !savedUrl.isEmpty()) {
            url.set(savedUrl);
        }

        String savedEmail = jiraAuthService.getSavedEmail();
        if (savedEmail != null && !savedEmail.isEmpty()) {
            email.set(savedEmail);
        }

        String savedToken = jiraAuthService.getSavedToken();
        if (savedToken != null && !savedToken.isEmpty()) {
            token.set(savedToken);
        }
    }

    public void connectAndTest() {
        if (url.get().isEmpty() || email.get().isEmpty() || token.get().isEmpty()) {
            statusMessage.set("Please fill in all fields.");
            isSuccess.set(false);
            return;
        }

        isConnecting.set(true);
        statusMessage.set("Connecting...");

        jiraAuthService.testConnection(url.get(), email.get(), token.get())
                .thenAccept(success -> Platform.runLater(() -> {
                    isConnecting.set(false);
                    isSuccess.set(success);
                    if (success) {
                        statusMessage.set("Successfully connected to Jira!");
                    } else {
                        statusMessage.set("Connection failed. Check credentials.");
                    }
                }));
    }

    // Getters and properties
    public StringProperty urlProperty() { return url; }
    public StringProperty emailProperty() { return email; }
    public StringProperty tokenProperty() { return token; }
    public BooleanProperty isConnectingProperty() { return isConnecting; }
    public StringProperty statusMessageProperty() { return statusMessage; }
    public BooleanProperty isSuccessProperty() { return isSuccess; }
}
