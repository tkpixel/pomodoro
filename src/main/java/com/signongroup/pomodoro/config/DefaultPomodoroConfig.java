package com.signongroup.pomodoro.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.bind.annotation.Bindable;

@ConfigurationProperties("pomodoro.defaults")
public record DefaultPomodoroConfig(
    @Bindable(defaultValue = "25") int focusDurationMinutes,
    @Bindable(defaultValue = "5") int shortBreakDurationMinutes,
    @Bindable(defaultValue = "15") int longBreakDurationMinutes,
    @Bindable(defaultValue = "4") int cyclesBeforeLongBreak
) {}
