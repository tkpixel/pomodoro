package com.signongroup.pomodoro.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.signongroup.pomodoro.config.DefaultPomodoroConfig;
import com.signongroup.pomodoro.model.DurationSettings;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

@Singleton
public class JsonSettingsService implements SettingsService {

    private static final Logger log = LoggerFactory.getLogger(JsonSettingsService.class);
    private static final String SETTINGS_DIR_NAME = ".monolith";
    private static final String SETTINGS_FILE_NAME = "settings.json";

    private final ObjectMapper objectMapper;
    private final File settingsFile;
    private final DefaultPomodoroConfig defaultConfig;

    @Inject
    public JsonSettingsService(DefaultPomodoroConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
        this.objectMapper = new ObjectMapper();

        String userHome = System.getProperty("user.home");
        File settingsDir = new File(userHome, SETTINGS_DIR_NAME);
        if (!settingsDir.exists()) {
            boolean created = settingsDir.mkdirs();
            if (!created) {
                log.warn("Failed to create settings directory at: {}", settingsDir.getAbsolutePath());
            }
        }
        this.settingsFile = new File(settingsDir, SETTINGS_FILE_NAME);
    }

    @Override
    public DurationSettings loadSettings() {
        if (settingsFile.exists()) {
            try {
                return objectMapper.readValue(settingsFile, DurationSettings.class);
            } catch (IOException e) {
                log.error("Failed to load settings from JSON. Using defaults.", e);
            }
        }
        return new DurationSettings(
                defaultConfig.focusDurationMinutes(),
                defaultConfig.shortBreakDurationMinutes(),
                defaultConfig.longBreakDurationMinutes(),
                defaultConfig.cyclesBeforeLongBreak(),
                false, // autoStartBreaks default
                true, // enableSessionSound default
                true // enableBreakSound default
        );
    }

    @Override
    public void saveSettings(DurationSettings settings) {
        try {
            objectMapper.writeValue(settingsFile, settings);
            log.debug("Settings successfully saved to {}", settingsFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to save settings to JSON.", e);
        }
    }
}
