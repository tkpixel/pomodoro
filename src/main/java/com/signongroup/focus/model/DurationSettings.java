package com.signongroup.focus.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DurationSettings(
    @JsonProperty("focusSessionMinutes") int focusSessionMinutes,
    @JsonProperty("shortBreakMinutes") int shortBreakMinutes,
    @JsonProperty("longBreakMinutes") int longBreakMinutes,
    @JsonProperty("maxSessionCount") int maxSessionCount,
    @JsonProperty("autoStartBreaks") boolean autoStartBreaks,
    @JsonProperty("autoStartSessions") Boolean autoStartSessions,
    @JsonProperty("enableSessionSound") Boolean enableSessionSound,
    @JsonProperty("enableBreakSound") Boolean enableBreakSound,
    @JsonProperty("activeProfile") String activeProfile
) {
}
