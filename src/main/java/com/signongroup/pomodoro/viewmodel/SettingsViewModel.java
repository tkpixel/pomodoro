package com.signongroup.pomodoro.viewmodel;

import com.signongroup.pomodoro.model.DurationSettings;
import com.signongroup.pomodoro.service.JiraAuthService;
import com.signongroup.pomodoro.service.SettingsService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

@Singleton
public class SettingsViewModel {

    private final SettingsService settingsService;
    private final JiraAuthService jiraAuthService;

    // Accordion State
    private final BooleanProperty isGeneralExpanded = new SimpleBooleanProperty(false);
    private final BooleanProperty isDurationExpanded = new SimpleBooleanProperty(false);
    private final BooleanProperty isJiraExpanded = new SimpleBooleanProperty(false);

    // Duration Settings
    private final IntegerProperty focusSessionMinutes = new SimpleIntegerProperty();
    private final IntegerProperty shortBreakMinutes = new SimpleIntegerProperty();
    private final IntegerProperty longBreakMinutes = new SimpleIntegerProperty();
    private final IntegerProperty maxSessionCount = new SimpleIntegerProperty();
    private final BooleanProperty autoStartBreaks = new SimpleBooleanProperty();
    private final BooleanProperty enableSound = new SimpleBooleanProperty(true);

    // Jira Connection Settings
    private final StringProperty url = new SimpleStringProperty("");
    private final StringProperty email = new SimpleStringProperty("");
    private final StringProperty token = new SimpleStringProperty("");
    private final BooleanProperty isConnecting = new SimpleBooleanProperty(false);
    private final StringProperty statusMessage = new SimpleStringProperty("");
    private final BooleanProperty isSuccess = new SimpleBooleanProperty(false);

    // Validation
    private final BooleanProperty canConnect = new SimpleBooleanProperty(false);

    @Inject
    public SettingsViewModel(SettingsService settingsService, JiraAuthService jiraAuthService) {
        this.settingsService = settingsService;
        this.jiraAuthService = jiraAuthService;

        loadSettings();
        loadJiraCredentials();

        // Add listeners to auto-save when properties change
        focusSessionMinutes.addListener((obs, oldVal, newVal) -> saveSettings());
        shortBreakMinutes.addListener((obs, oldVal, newVal) -> saveSettings());
        longBreakMinutes.addListener((obs, oldVal, newVal) -> saveSettings());
        maxSessionCount.addListener((obs, oldVal, newVal) -> saveSettings());
        autoStartBreaks.addListener((obs, oldVal, newVal) -> saveSettings());
        enableSound.addListener((obs, oldVal, newVal) -> saveSettings());

        // Bind validation
        canConnect.bind(Bindings.createBooleanBinding(() ->
                !url.get().isEmpty() && !email.get().isEmpty() && !token.get().isEmpty() && !isConnecting.get(),
                url, email, token, isConnecting
        ));
    }

    private void loadJiraCredentials() {
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

    private void loadSettings() {
        DurationSettings settings = settingsService.loadSettings();
        focusSessionMinutes.set(settings.focusSessionMinutes());
        shortBreakMinutes.set(settings.shortBreakMinutes());
        longBreakMinutes.set(settings.longBreakMinutes());
        maxSessionCount.set(settings.maxSessionCount());
        autoStartBreaks.set(settings.autoStartBreaks());
    }

    private void saveSettings() {
        DurationSettings settings = new DurationSettings(
                focusSessionMinutes.get(),
                shortBreakMinutes.get(),
                longBreakMinutes.get(),
                maxSessionCount.get(),
                autoStartBreaks.get(),
                enableSound.get()
        );
        settingsService.saveSettings(settings);
    }

    /**
     * Toggles the general expanded state.
     */
    public void toggleGeneralExpanded() {
        isGeneralExpanded.set(!isGeneralExpanded.get());
        if (isGeneralExpanded.get()) {
            isDurationExpanded.set(false);
            isGeneralExpanded.set(false);
            isJiraExpanded.set(false);
            isGeneralExpanded.set(false);
        }
    }

    public void toggleDurationExpanded() {
        isDurationExpanded.set(!isDurationExpanded.get());
        if (isDurationExpanded.get()) {
            isJiraExpanded.set(false);
            isGeneralExpanded.set(false);
        }
    }

    public void toggleJiraExpanded() {
        isJiraExpanded.set(!isJiraExpanded.get());
        if (isJiraExpanded.get()) {
            isDurationExpanded.set(false);
            isGeneralExpanded.set(false);
        }
    }

    public void connectAndTestJira() {
        if (!canConnect.get()) {
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

    // --- Commands ---

    public void incrementFocusSession() {
        if (focusSessionMinutes.get() < 90) {
            focusSessionMinutes.set(focusSessionMinutes.get() + 5);
        }
    }

    public void decrementFocusSession() {
        if (focusSessionMinutes.get() > 5) {
            focusSessionMinutes.set(focusSessionMinutes.get() - 5);
        }
    }

    public void incrementShortBreak() {
        if (shortBreakMinutes.get() < 30) {
            shortBreakMinutes.set(shortBreakMinutes.get() + 1);
        }
    }

    public void decrementShortBreak() {
        if (shortBreakMinutes.get() > 1) {
            shortBreakMinutes.set(shortBreakMinutes.get() - 1);
        }
    }

    public void incrementLongBreak() {
        if (longBreakMinutes.get() < 60) {
            longBreakMinutes.set(longBreakMinutes.get() + 5);
        }
    }

    public void decrementLongBreak() {
        if (longBreakMinutes.get() > 5) {
            longBreakMinutes.set(longBreakMinutes.get() - 5);
        }
    }

    public void incrementMaxSessionCount() {
        if (maxSessionCount.get() < 10) {
            maxSessionCount.set(maxSessionCount.get() + 1);
        }
    }

    public void decrementMaxSessionCount() {
        if (maxSessionCount.get() > 1) {
            maxSessionCount.set(maxSessionCount.get() - 1);
        }
    }

    // --- Getters for Properties ---

    public BooleanProperty isDurationExpandedProperty() {
        return isDurationExpanded;
    }
    public BooleanProperty isJiraExpandedProperty() {
        return isJiraExpanded;
    }

    public IntegerProperty focusSessionMinutesProperty() {
        return focusSessionMinutes;
    }

    public IntegerProperty shortBreakMinutesProperty() {
        return shortBreakMinutes;
    }

    public IntegerProperty longBreakMinutesProperty() {
        return longBreakMinutes;
    }

    public IntegerProperty maxSessionCountProperty() {
        return maxSessionCount;
    }

    /**
     * Returns the isGeneralExpanded property.
     * @return BooleanProperty
     */
    public BooleanProperty isGeneralExpandedProperty() {
        return isGeneralExpanded;
    }
    /**
     * Returns the enableSound property.
     * @return BooleanProperty
     */
    public BooleanProperty enableSoundProperty() {
        return enableSound;
    }

    public BooleanProperty autoStartBreaksProperty() {
        return autoStartBreaks;
    }

    public StringProperty urlProperty() {
        return url;
    }
    public StringProperty emailProperty() {
        return email;
    }
    public StringProperty tokenProperty() {
        return token;
    }
    public BooleanProperty isConnectingProperty() {
        return isConnecting;
    }
    public StringProperty statusMessageProperty() {
        return statusMessage;
    }
    public BooleanProperty isSuccessProperty() {
        return isSuccess;
    }
    public BooleanProperty canConnectProperty() {
        return canConnect;
    }
}
