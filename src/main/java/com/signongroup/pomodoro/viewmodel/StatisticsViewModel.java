package com.signongroup.pomodoro.viewmodel;

import com.signongroup.pomodoro.service.JiraBoardService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.concurrent.CompletableFuture;

@Singleton
public class StatisticsViewModel {

    private final IntegerProperty clearedToday = new SimpleIntegerProperty();
    private final IntegerProperty week = new SimpleIntegerProperty();
    private final IntegerProperty sprintCleared = new SimpleIntegerProperty();
    private final IntegerProperty sprintPlanned = new SimpleIntegerProperty();
    private final IntegerProperty year = new SimpleIntegerProperty();
    private final BooleanProperty isLoading = new SimpleBooleanProperty();

    private final JiraBoardService jiraBoardService;

    @Inject
    public StatisticsViewModel(JiraBoardService jiraBoardService) {
        this.jiraBoardService = jiraBoardService;
        // Default to 0 instead of mock values initially
        this.clearedToday.set(0);
        this.week.set(0);
        this.sprintCleared.set(0);
        this.sprintPlanned.set(0);
        this.year.set(0);
    }

    public void loadStatistics() {
        Platform.runLater(() -> isLoading.set(true));

        String jqlToday = "assignee = currentUser() AND statusCategory = Done AND statusCategoryChangedDate >= startOfDay()";
        String jqlWeek = "assignee = currentUser() AND statusCategory = Done AND statusCategoryChangedDate >= startOfWeek()";
        String jqlYear = "assignee = currentUser() AND statusCategory = Done AND statusCategoryChangedDate >= startOfYear()";
        String jqlSprintCleared = "assignee = currentUser() AND sprint in openSprints() AND statusCategory = Done";
        String jqlSprintPlanned = "assignee = currentUser() AND sprint in openSprints()";

        CompletableFuture<Integer> todayFuture = jiraBoardService.fetchTicketCount(jqlToday).exceptionally(ex -> 0);
        CompletableFuture<Integer> weekFuture = jiraBoardService.fetchTicketCount(jqlWeek).exceptionally(ex -> 0);
        CompletableFuture<Integer> yearFuture = jiraBoardService.fetchTicketCount(jqlYear).exceptionally(ex -> 0);
        CompletableFuture<Integer> sprintClearedFuture = jiraBoardService.fetchTicketCount(jqlSprintCleared).exceptionally(ex -> 0);
        CompletableFuture<Integer> sprintPlannedFuture = jiraBoardService.fetchTicketCount(jqlSprintPlanned).exceptionally(ex -> 0);

        CompletableFuture.allOf(todayFuture, weekFuture, yearFuture, sprintClearedFuture, sprintPlannedFuture)
                .whenComplete((v, ex) -> {
                    if (ex != null) {
                        System.err.println("Error loading statistics: " + ex.getMessage());
                    }
                    int todayCount = todayFuture.join();
                    int weekCount = weekFuture.join();
                    int yearCount = yearFuture.join();
                    int sprintClearedCount = sprintClearedFuture.join();
                    int sprintPlannedCount = sprintPlannedFuture.join();

                    Platform.runLater(() -> {
                        this.clearedToday.set(todayCount);
                        this.week.set(weekCount);
                        this.year.set(yearCount);
                        this.sprintCleared.set(sprintClearedCount);
                        this.sprintPlanned.set(sprintPlannedCount);
                        this.isLoading.set(false);
                    });
                });
    }

    public BooleanProperty isLoadingProperty() {
        return isLoading;
    }

    public boolean isIsLoading() {
        return isLoading.get();
    }

    public void setIsLoading(boolean isLoading) {
        this.isLoading.set(isLoading);
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
