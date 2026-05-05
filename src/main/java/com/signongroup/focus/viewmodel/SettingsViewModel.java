package com.signongroup.focus.viewmodel;

import com.signongroup.focus.model.DurationSettings;
import com.signongroup.focus.service.JiraAuthService;
import com.signongroup.focus.service.SettingsService;
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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import com.signongroup.focus.model.FocusProfile;

@Singleton
public class SettingsViewModel {

    private final SettingsService settingsService;
    private final JiraAuthService jiraAuthService;

    private final ObjectProperty<FocusProfile> selectedProfile = new SimpleObjectProperty<>(FocusProfile.POMODORO);
    private final StringProperty profileDescription = new SimpleStringProperty("");
    private boolean isUpdatingFromProfile = false;

    // Accordion State
    private final BooleanProperty isDurationExpanded = new SimpleBooleanProperty(false);
    private final BooleanProperty isJiraExpanded = new SimpleBooleanProperty(false);

    // Duration Settings
    private final IntegerProperty focusSessionMinutes = new SimpleIntegerProperty();
    private final IntegerProperty shortBreakMinutes = new SimpleIntegerProperty();
    private final IntegerProperty longBreakMinutes = new SimpleIntegerProperty();
    private final IntegerProperty maxSessionCount = new SimpleIntegerProperty();
    private final BooleanProperty autoStartBreaks = new SimpleBooleanProperty();
    private final BooleanProperty autoStartSessions = new SimpleBooleanProperty();
    private final BooleanProperty enableSessionSound = new SimpleBooleanProperty(true);
    private final BooleanProperty enableBreakSound = new SimpleBooleanProperty(true);

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

        selectedProfile.addListener((obs, oldVal, newVal) -> {
            if (!isUpdatingFromProfile) {
                isUpdatingFromProfile = true;
                focusSessionMinutes.set(newVal.getFocusMinutes());
                shortBreakMinutes.set(newVal.getShortBreakMinutes());
                longBreakMinutes.set(newVal.getLongBreakMinutes());
                maxSessionCount.set(newVal.getIntervals());
                isUpdatingFromProfile = false;
            }
            updateProfileDescription(newVal);
            saveSettings();
        });

        // Add listeners to auto-save when properties change
        focusSessionMinutes.addListener((obs, oldVal, newVal) -> {
            if (!isUpdatingFromProfile && selectedProfile.get() != FocusProfile.CUSTOM) {
                isUpdatingFromProfile = true;
                selectedProfile.set(FocusProfile.CUSTOM);
                isUpdatingFromProfile = false;
            }
            saveSettings();
        });
        shortBreakMinutes.addListener((obs, oldVal, newVal) -> {
            if (!isUpdatingFromProfile && selectedProfile.get() != FocusProfile.CUSTOM) {
                isUpdatingFromProfile = true;
                selectedProfile.set(FocusProfile.CUSTOM);
                isUpdatingFromProfile = false;
            }
            saveSettings();
        });
        longBreakMinutes.addListener((obs, oldVal, newVal) -> {
            if (!isUpdatingFromProfile && selectedProfile.get() != FocusProfile.CUSTOM) {
                isUpdatingFromProfile = true;
                selectedProfile.set(FocusProfile.CUSTOM);
                isUpdatingFromProfile = false;
            }
            saveSettings();
        });
        maxSessionCount.addListener((obs, oldVal, newVal) -> {
            if (!isUpdatingFromProfile && selectedProfile.get() != FocusProfile.CUSTOM) {
                isUpdatingFromProfile = true;
                selectedProfile.set(FocusProfile.CUSTOM);
                isUpdatingFromProfile = false;
            }
            saveSettings();
        });
        autoStartBreaks.addListener((obs, oldVal, newVal) -> saveSettings());
        autoStartSessions.addListener((obs, oldVal, newVal) -> saveSettings());
        enableSessionSound.addListener((obs, oldVal, newVal) -> saveSettings());
        enableBreakSound.addListener((obs, oldVal, newVal) -> saveSettings());

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

    private void updateProfileDescription(FocusProfile profile) {
        switch (profile) {
            case POMODORO:
                profileDescription.set("The classic Pomodoro technique uses 25-minute work intervals followed by 5-minute "
                        + "short breaks. After four consecutive cycles, you take a longer 15-minute break to fully recharge. "
                        + "This profile is perfect for tackling a series of smaller tasks, maintaining a steady rhythm, "
                        + "and effectively overcoming procrastination.");
                break;
            case RULE_52_17:
                profileDescription.set("Based on research analyzing the habits of highly productive individuals, this profile "
                        + "champions 52 minutes of intense work followed by a 17-minute break. This specific ratio prevents "
                        + "cognitive fatigue while allowing for a truly meaningful recovery period. It is an excellent choice "
                        + "for structured daily routines and sustaining high focus throughout the entire workday.");
                break;
            case DEEP_WORK:
                profileDescription.set("Inspired by the human body's natural ultradian rhythms, this profile locks you in "
                        + "for 90 minutes of absolute, uninterrupted focus. It is specifically designed for complex "
                        + "problem-solving, deep coding sessions, or demanding creative work. Extended breaks of 20 to "
                        + "30 minutes ensure your brain has enough time to fully recover between these heavy cognitive sprints.");
                break;
            case CUSTOM:
                profileDescription.set("Take full control of your temporal workflow by defining your own customized "
                        + "intervals. Whether you need a quick 10-minute sprint or an unconventional marathon session, "
                        + "this profile adapts entirely to your unique requirements. It empowers you to fine-tune your "
                        + "focus and break durations exactly how your current task or environment demands.");
                break;
            default:
                profileDescription.set("");
                break;
        }
    }

    private void loadSettings() {
        DurationSettings settings = settingsService.loadSettings();
        if (settings.activeProfile() != null) {
            selectedProfile.set(FocusProfile.valueOf(settings.activeProfile()));
        } else {
            selectedProfile.set(FocusProfile.POMODORO);
        }
        updateProfileDescription(selectedProfile.get());
        focusSessionMinutes.set(settings.focusSessionMinutes());
        shortBreakMinutes.set(settings.shortBreakMinutes());
        longBreakMinutes.set(settings.longBreakMinutes());
        maxSessionCount.set(settings.maxSessionCount());
        autoStartBreaks.set(settings.autoStartBreaks());
        if (settings.autoStartSessions() != null) {
            autoStartSessions.set(settings.autoStartSessions());
        } else {
            autoStartSessions.set(false);
        }
        if (settings.enableSessionSound() != null) {
            enableSessionSound.set(settings.enableSessionSound());
        } else {
            enableSessionSound.set(true);
        }
        if (settings.enableBreakSound() != null) {
            enableBreakSound.set(settings.enableBreakSound());
        } else {
            enableBreakSound.set(true);
        }
    }

    private void saveSettings() {
        DurationSettings settings = new DurationSettings(
                focusSessionMinutes.get(),
                shortBreakMinutes.get(),
                longBreakMinutes.get(),
                maxSessionCount.get(),
                autoStartBreaks.get(),
                autoStartSessions.get(),
                enableSessionSound.get(),
                enableBreakSound.get(),
                selectedProfile.get().name()
        );
        settingsService.saveSettings(settings);
    }

    public void toggleDurationExpanded() {
        isDurationExpanded.set(!isDurationExpanded.get());
        if (isDurationExpanded.get()) {
            isJiraExpanded.set(false);
        }
    }

    public void toggleJiraExpanded() {
        isJiraExpanded.set(!isJiraExpanded.get());
        if (isJiraExpanded.get()) {
            isDurationExpanded.set(false);
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
        int max = selectedProfile.get() == FocusProfile.CUSTOM ? 180 : 90;
        if (focusSessionMinutes.get() < max) {
            focusSessionMinutes.set(focusSessionMinutes.get() + 1);
        }
    }

    public void decrementFocusSession() {
        if (focusSessionMinutes.get() > 1) {
            focusSessionMinutes.set(focusSessionMinutes.get() - 1);
        }
    }

    public void incrementShortBreak() {
        int max = selectedProfile.get() == FocusProfile.CUSTOM ? 60 : 30;
        if (shortBreakMinutes.get() < max) {
            shortBreakMinutes.set(shortBreakMinutes.get() + 1);
        }
    }

    public void decrementShortBreak() {
        if (shortBreakMinutes.get() > 1) {
            shortBreakMinutes.set(shortBreakMinutes.get() - 1);
        }
    }

    public void incrementLongBreak() {
        int max = selectedProfile.get() == FocusProfile.CUSTOM ? 120 : 60;
        if (longBreakMinutes.get() < max) {
            longBreakMinutes.set(longBreakMinutes.get() + 5);
        }
    }

    public void decrementLongBreak() {
        if (longBreakMinutes.get() > 5) {
            longBreakMinutes.set(longBreakMinutes.get() - 5);
        }
    }

    public void incrementMaxSessionCount() {
        int max = selectedProfile.get() == FocusProfile.CUSTOM ? 20 : 10;
        if (maxSessionCount.get() < max) {
            maxSessionCount.set(maxSessionCount.get() + 1);
        }
    }

    public void decrementMaxSessionCount() {
        if (maxSessionCount.get() > 1) {
            maxSessionCount.set(maxSessionCount.get() - 1);
        }
    }

    // --- Getters for Properties ---

    /**
     * Gets the selected profile property.
     * @return the selected profile property
     */
    public ObjectProperty<FocusProfile> selectedProfileProperty() {
        return selectedProfile;
    }

    /**
     * Gets the profile description property.
     * @return the profile description property
     */
    public StringProperty profileDescriptionProperty() {
        return profileDescription;
    }

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
     * Returns the enableSessionSound property.
     * @return BooleanProperty
     */
    public BooleanProperty enableSessionSoundProperty() {
        return enableSessionSound;
    }

    /**
     * Returns the enableBreakSound property.
     * @return BooleanProperty
     */
    public BooleanProperty enableBreakSoundProperty() {
        return enableBreakSound;
    }

    public BooleanProperty autoStartSessionsProperty() {
        return autoStartSessions;
    }

    /**
     * Returns the autoStartBreaks property.
     * @return BooleanProperty
     */
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
