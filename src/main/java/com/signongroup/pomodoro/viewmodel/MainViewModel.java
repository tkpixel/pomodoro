package com.signongroup.pomodoro.viewmodel;

import jakarta.inject.Singleton;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * ViewModel für die Hauptansicht (MVVM-Pattern).
 * Wird von Micronaut als Singleton verwaltet.
 */
@Singleton
public class MainViewModel {

    private final StringProperty timerText = new SimpleStringProperty("25:00");
    private final StringProperty sessionText = new SimpleStringProperty("1 / 4");
    private final StringProperty clearedTodayText = new SimpleStringProperty("00");
    private final StringProperty nextBreakText = new SimpleStringProperty("05:00");
    private final DoubleProperty timerProgress = new SimpleDoubleProperty(0.65); // 65% progress to match visual

    public StringProperty timerTextProperty() {
        return timerText;
    }

    public String getTimerText() {
        return timerText.get();
    }

    public void setTimerText(String value) {
        this.timerText.set(value);
    }

    public StringProperty sessionTextProperty() {
        return sessionText;
    }

    public String getSessionText() {
        return sessionText.get();
    }

    public void setSessionText(String value) {
        this.sessionText.set(value);
    }

    public StringProperty clearedTodayTextProperty() {
        return clearedTodayText;
    }

    public String getClearedTodayText() {
        return clearedTodayText.get();
    }

    public void setClearedTodayText(String value) {
        this.clearedTodayText.set(value);
    }

    public StringProperty nextBreakTextProperty() {
        return nextBreakText;
    }

    public String getNextBreakText() {
        return nextBreakText.get();
    }

    public void setNextBreakText(String value) {
        this.nextBreakText.set(value);
    }

    public DoubleProperty timerProgressProperty() {
        return timerProgress;
    }

    public double getTimerProgress() {
        return timerProgress.get();
    }

    public void setTimerProgress(double value) {
        this.timerProgress.set(value);
    }
}
