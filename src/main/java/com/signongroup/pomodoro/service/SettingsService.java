package com.signongroup.pomodoro.service;

import com.signongroup.pomodoro.model.DurationSettings;

public interface SettingsService {
  DurationSettings loadSettings();

  void saveSettings(DurationSettings settings);
}
