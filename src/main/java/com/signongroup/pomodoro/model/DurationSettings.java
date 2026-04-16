package com.signongroup.pomodoro.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DurationSettings(
    @JsonProperty("focusSessionMinutes") int focusSessionMinutes,
    @JsonProperty("shortBreakMinutes") int shortBreakMinutes,
    @JsonProperty("longBreakMinutes") int longBreakMinutes,
    @JsonProperty("maxSessionCount") int maxSessionCount,
    @JsonProperty("autoStartBreaks") boolean autoStartBreaks
) {
    public static DurationSettings defaultSettings() {
        return new DurationSettings(25, 5, 15, 4, false);
    }
}
