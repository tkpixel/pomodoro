package com.signongroup.focus.service;

import com.signongroup.focus.model.DurationSettings;

public interface SettingsService {
    DurationSettings loadSettings();
    void saveSettings(DurationSettings settings);
}
