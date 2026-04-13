package com.signongroup.pomodoro.viewmodel;

import jakarta.inject.Singleton;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

@Singleton
public class StatisticsViewModel {

    private final IntegerProperty clearedToday = new SimpleIntegerProperty();
    private final IntegerProperty week = new SimpleIntegerProperty();
    private final IntegerProperty sprintCleared = new SimpleIntegerProperty();
    private final IntegerProperty sprintPlanned = new SimpleIntegerProperty();
    private final IntegerProperty year = new SimpleIntegerProperty();

    public StatisticsViewModel() {
        // Mocked default values
        this.clearedToday.set(4);
        this.week.set(28);
        this.sprintCleared.set(124);
        this.sprintPlanned.set(200);
        this.year.set(850);
    }

    public IntegerProperty clearedTodayProperty() {
        return clearedToday;
    }

    public int getClearedToday() {
        return clearedToday.get();
    }

    public void setClearedToday(int clearedToday) {
        this.clearedToday.set(clearedToday);
    }

    public IntegerProperty weekProperty() {
        return week;
    }

    public int getWeek() {
        return week.get();
    }

    public void setWeek(int week) {
        this.week.set(week);
    }

    public IntegerProperty sprintClearedProperty() {
        return sprintCleared;
    }

    public int getSprintCleared() {
        return sprintCleared.get();
    }

    public void setSprintCleared(int sprintCleared) {
        this.sprintCleared.set(sprintCleared);
    }

    public IntegerProperty sprintPlannedProperty() {
        return sprintPlanned;
    }

    public int getSprintPlanned() {
        return sprintPlanned.get();
    }

    public void setSprintPlanned(int sprintPlanned) {
        this.sprintPlanned.set(sprintPlanned);
    }

    public IntegerProperty yearProperty() {
        return year;
    }

    public int getYear() {
        return year.get();
    }

    public void setYear(int year) {
        this.year.set(year);
    }
}
