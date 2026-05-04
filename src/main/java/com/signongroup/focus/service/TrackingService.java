package com.signongroup.focus.service;

import jakarta.inject.Singleton;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

@Singleton
public class TrackingService {

    public enum TrackingMode {
        POMODORO,
        STOPWATCH
    }

    private final StringProperty activeTime = new SimpleStringProperty("00:00");
    private final BooleanProperty isRunning = new SimpleBooleanProperty(false);
    private final javafx.beans.property.ObjectProperty<TrackingMode> activeMode = new javafx.beans.property.SimpleObjectProperty<>(TrackingMode.POMODORO);

    private Runnable onToggleTimer;
    private Runnable onResetTimer;

    public StringProperty activeTimeProperty() {
        return activeTime;
    }

    public BooleanProperty isRunningProperty() {
        return isRunning;
    }

    public TrackingMode getActiveMode() {
        return activeMode.get();
    }

    public void setActiveMode(TrackingMode mode) {
        this.activeMode.set(mode);
    }

    public javafx.beans.property.ObjectProperty<TrackingMode> activeModeProperty() {
        return activeMode;
    }

    public void setOnToggleTimer(Runnable onToggleTimer) {
        this.onToggleTimer = onToggleTimer;
    }

    public void setOnResetTimer(Runnable onResetTimer) {
        this.onResetTimer = onResetTimer;
    }

    public void toggleTimer() {
        if (onToggleTimer != null) {
            onToggleTimer.run();
        }
    }

    public void resetTimer() {
        if (onResetTimer != null) {
            onResetTimer.run();
        }
    }
}
