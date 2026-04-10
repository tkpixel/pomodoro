package com.signongroup.pomodoro.viewmodel;

import jakarta.inject.Singleton;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.util.Duration;

/**
 * ViewModel für die Hauptansicht (MVVM-Pattern).
 * Wird von Micronaut als Singleton verwaltet.
 */
@Singleton
public class MainViewModel {

    public enum TimerState {
        READY,
        FOCUS_RUNNING,
        BREAK_RUNNING
    }

    private final StringProperty timerText = new SimpleStringProperty("25:00");
    private final StringProperty sessionText = new SimpleStringProperty("1 / 4");
    private final StringProperty clearedTodayText = new SimpleStringProperty("00");
    private final StringProperty nextBreakText = new SimpleStringProperty("05:00");
    private final DoubleProperty timerProgress = new SimpleDoubleProperty(1.0);
    private final DoubleProperty breakProgress = new SimpleDoubleProperty(0.0);
    private final BooleanProperty isRunning = new SimpleBooleanProperty(false);
    private final ObjectProperty<TimerState> timerState = new SimpleObjectProperty<>(TimerState.READY);

    private int focusTimeSeconds = 25 * 60;
    private int shortBreakSeconds = 5 * 60;
    private int longBreakSeconds = 15 * 60;
    private int maxSessions = 4;

    private int currentSession = 1;
    private int clearedToday = 0;
    private int timeRemainingSeconds = focusTimeSeconds;

    private Timeline timeline;

    public MainViewModel() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick()));
        timeline.setCycleCount(Animation.INDEFINITE);
        updateUI();
    }

    private void tick() {
        if (timeRemainingSeconds > 0) {
            timeRemainingSeconds--;
            updateUI();
        } else {
            handleTimerComplete();
        }
    }

    private void handleTimerComplete() {
        pauseTimer();
        if (timerState.get() == TimerState.FOCUS_RUNNING) {
            clearedToday++;
            if (currentSession < maxSessions) {
                currentSession++;
                timeRemainingSeconds = shortBreakSeconds;
            } else {
                currentSession = 1;
                timeRemainingSeconds = longBreakSeconds;
            }
            timerState.set(TimerState.BREAK_RUNNING);
            updateUI();
            startTimer();
        } else if (timerState.get() == TimerState.BREAK_RUNNING) {
            resetToReady();
        }
    }

    private void updateUI() {
        if (timerState.get() == TimerState.BREAK_RUNNING) {
            int maxTime = (currentSession == 1 && clearedToday > 0) ? longBreakSeconds : shortBreakSeconds;
            nextBreakText.set(formatTime(timeRemainingSeconds));
            breakProgress.set((double) timeRemainingSeconds / maxTime);
            timerText.set("00:00");
            timerProgress.set(0.0);
        } else {
            timerText.set(formatTime(timeRemainingSeconds));
            timerProgress.set((double) timeRemainingSeconds / focusTimeSeconds);
            int maxTime = (currentSession == maxSessions) ? longBreakSeconds : shortBreakSeconds;
            nextBreakText.set(formatTime(maxTime));
            breakProgress.set(0.0);
        }
        sessionText.set(currentSession + " / " + maxSessions);
        clearedTodayText.set(String.format("%02d", clearedToday));
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public void startTimer() {
        if (timerState.get() == TimerState.READY) {
            timerState.set(TimerState.FOCUS_RUNNING);
        }
        isRunning.set(true);
        timeline.play();
    }

    public void pauseTimer() {
        isRunning.set(false);
        timeline.pause();
    }

    public void toggleTimer() {
        if (isRunning.get()) {
            pauseTimer();
        } else {
            startTimer();
        }
    }

    public void skipBreak() {
        if (timerState.get() == TimerState.BREAK_RUNNING) {
            pauseTimer();
            resetToReady();
        }
    }

    private void resetToReady() {
        timerState.set(TimerState.READY);
        timeRemainingSeconds = focusTimeSeconds;
        updateUI();
    }

    // --- Getters & Setters / Properties ---

    public StringProperty timerTextProperty() { return timerText; }
    public String getTimerText() { return timerText.get(); }
    public void setTimerText(String value) { this.timerText.set(value); }

    public StringProperty sessionTextProperty() { return sessionText; }
    public String getSessionText() { return sessionText.get(); }
    public void setSessionText(String value) { this.sessionText.set(value); }

    public StringProperty clearedTodayTextProperty() { return clearedTodayText; }
    public String getClearedTodayText() { return clearedTodayText.get(); }
    public void setClearedTodayText(String value) { this.clearedTodayText.set(value); }

    public StringProperty nextBreakTextProperty() { return nextBreakText; }
    public String getNextBreakText() { return nextBreakText.get(); }
    public void setNextBreakText(String value) { this.nextBreakText.set(value); }

    public DoubleProperty timerProgressProperty() { return timerProgress; }
    public double getTimerProgress() { return timerProgress.get(); }
    public void setTimerProgress(double value) { this.timerProgress.set(value); }

    public DoubleProperty breakProgressProperty() { return breakProgress; }
    public double getBreakProgress() { return breakProgress.get(); }
    public void setBreakProgress(double value) { this.breakProgress.set(value); }

    public BooleanProperty isRunningProperty() { return isRunning; }
    public boolean getIsRunning() { return isRunning.get(); }

    public ObjectProperty<TimerState> timerStateProperty() { return timerState; }
    public TimerState getTimerState() { return timerState.get(); }

}
