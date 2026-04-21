package com.signongroup.pomodoro.viewmodel;

import com.signongroup.pomodoro.model.jira.JiraBoard;
import com.signongroup.pomodoro.service.JiraBoardService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.concurrent.CompletableFuture;

@Singleton
public class StatisticsViewModel {

    private static final String JQL_TODAY = "assignee = currentUser() AND statusCategory = Done AND project = \"%s\" AND statusCategoryChangedDate >= startOfDay()";
    private static final String JQL_WEEK = "assignee = currentUser() AND statusCategory = Done AND project = \"%s\" AND statusCategoryChangedDate >= startOfWeek()";
    private static final String JQL_SPRINT_CLEARED = "assignee = currentUser() AND sprint in openSprints() AND statusCategory = Done AND project = \"%s\" AND statusCategoryChangedDate >= startOfYear()";
    private static final String JQL_SPRINT_TOTAL = "assignee = currentUser() AND sprint in openSprints() AND project = \"%s\"";
    private static final String JQL_OVERALL_YEAR = "assignee = currentUser() AND statusCategory = Done AND statusCategoryChangedDate >= startOfYear()";

    private final JiraBoardService jiraBoardService;

    private final IntegerProperty clearedToday = new SimpleIntegerProperty();
    private final IntegerProperty week = new SimpleIntegerProperty();
    private final IntegerProperty sprintCleared = new SimpleIntegerProperty();
    private final IntegerProperty sprintPlanned = new SimpleIntegerProperty();
    private final IntegerProperty year = new SimpleIntegerProperty();

    @Inject
    public StatisticsViewModel(JiraBoardService jiraBoardService) {
        this.jiraBoardService = jiraBoardService;
        this.clearedToday.set(0);
        this.week.set(0);
        this.sprintCleared.set(0);
        this.sprintPlanned.set(0);
        this.year.set(0);
    }

    public void loadStatistics(JiraBoard selectedBoard) {
        CompletableFuture<Integer> yearFuture = jiraBoardService.fetchTicketCount(JQL_OVERALL_YEAR);

        String projectKey = (selectedBoard != null && selectedBoard.location() != null)
                ? selectedBoard.location().projectKey()
                : null;

        if (projectKey != null && !projectKey.isBlank()) {
            CompletableFuture<Integer> todayFuture = jiraBoardService.fetchTicketCount(String.format(JQL_TODAY, projectKey));
            CompletableFuture<Integer> weekFuture = jiraBoardService.fetchTicketCount(String.format(JQL_WEEK, projectKey));
            CompletableFuture<Integer> sprintClearedFuture = jiraBoardService.fetchTicketCount(String.format(JQL_SPRINT_CLEARED, projectKey));
            CompletableFuture<Integer> sprintTotalFuture = jiraBoardService.fetchTicketCount(String.format(JQL_SPRINT_TOTAL, projectKey));

            CompletableFuture.allOf(yearFuture, todayFuture, weekFuture, sprintClearedFuture, sprintTotalFuture)
                .thenAccept(v -> {
                    int finalYear = yearFuture.join();
                    int finalToday = todayFuture.join();
                    int finalWeek = weekFuture.join();
                    int finalSprintCleared = sprintClearedFuture.join();
                    int finalSprintTotal = sprintTotalFuture.join();

                    Platform.runLater(() -> {
                        year.set(finalYear);
                        clearedToday.set(finalToday);
                        week.set(finalWeek);
                        sprintCleared.set(finalSprintCleared);
                        sprintPlanned.set(finalSprintTotal);
                    });
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
        } else {
            yearFuture.thenAccept(finalYear -> {
                Platform.runLater(() -> {
                    year.set(finalYear);
                    clearedToday.set(0);
                    week.set(0);
                    sprintCleared.set(0);
                    sprintPlanned.set(0);
                });
            }).exceptionally(ex -> {
                ex.printStackTrace();
                return null;
            });
        }
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
