package com.signongroup.pomodoro.viewmodel;

import com.signongroup.pomodoro.model.DurationSettings;
import com.signongroup.pomodoro.service.SettingsService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

@Singleton
public class SettingsViewModel {

    private final SettingsService settingsService;

    private final IntegerProperty focusSessionMinutes = new SimpleIntegerProperty();
    private final IntegerProperty shortBreakMinutes = new SimpleIntegerProperty();
    private final IntegerProperty longBreakMinutes = new SimpleIntegerProperty();
    private final IntegerProperty maxSessionCount = new SimpleIntegerProperty();

    @Inject
    public SettingsViewModel(SettingsService settingsService) {
        this.settingsService = settingsService;
        loadSettings();

        // Add listeners to auto-save when properties change
        focusSessionMinutes.addListener((obs, oldVal, newVal) -> saveSettings());
        shortBreakMinutes.addListener((obs, oldVal, newVal) -> saveSettings());
        longBreakMinutes.addListener((obs, oldVal, newVal) -> saveSettings());
        maxSessionCount.addListener((obs, oldVal, newVal) -> saveSettings());
    }

    private void loadSettings() {
        DurationSettings settings = settingsService.loadSettings();
        focusSessionMinutes.set(settings.focusSessionMinutes());
        shortBreakMinutes.set(settings.shortBreakMinutes());
        longBreakMinutes.set(settings.longBreakMinutes());
        maxSessionCount.set(settings.maxSessionCount());
    }

    private void saveSettings() {
        DurationSettings settings = new DurationSettings(
                focusSessionMinutes.get(),
                shortBreakMinutes.get(),
                longBreakMinutes.get(),
                maxSessionCount.get()
        );
        settingsService.saveSettings(settings);
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
}
