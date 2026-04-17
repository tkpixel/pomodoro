package com.signongroup.pomodoro.viewmodel;

import com.signongroup.pomodoro.model.jira.JiraTask;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TaskCardViewModel {

    private final JiraTask task;

    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty taskKey = new SimpleStringProperty();
    private final StringProperty epic = new SimpleStringProperty();
    private final StringProperty assigneeInitials = new SimpleStringProperty();
    private final StringProperty priorityIconLiteral = new SimpleStringProperty();
    private final StringProperty priorityIconColor = new SimpleStringProperty();
    private final StringProperty issueTypeIconUrl = new SimpleStringProperty();
    private final StringProperty storyPoints = new SimpleStringProperty();
    private final BooleanProperty isCompleted = new SimpleBooleanProperty();
    private final BooleanProperty isInProgress = new SimpleBooleanProperty();
    private final DoubleProperty progress = new SimpleDoubleProperty();
    private final StringProperty progressPercentage = new SimpleStringProperty();
    private final BooleanProperty hasProgress = new SimpleBooleanProperty();
    private final BooleanProperty hasStoryPoints = new SimpleBooleanProperty();
    private final BooleanProperty hasEpic = new SimpleBooleanProperty();
    private final BooleanProperty hasAssignee = new SimpleBooleanProperty();
    private final BooleanProperty hasIssueTypeIcon = new SimpleBooleanProperty();

    private long currentTimeSpentSeconds = 0;
    private long currentEstimateSeconds = 0;

    public TaskCardViewModel(JiraTask task) {
        this.task = task;
        updateProperties();
    }

    private void updateProperties() {
        if (task == null) return;

        taskKey.set(task.key());

        if (task.fields() == null) return;

        title.set(task.fields().summary() != null ? task.fields().summary() : "Untitled Task");

        String epicKey = task.fields().epicKey();
        epic.set(epicKey);
        hasEpic.set(epicKey != null && !epicKey.isBlank());

        if (task.fields().assignee() != null && task.fields().assignee().displayName() != null) {
            assigneeInitials.set(getInitials(task.fields().assignee().displayName()));
            hasAssignee.set(true);
        } else {
            hasAssignee.set(false);
        }

        if (task.fields().issuetype() != null && task.fields().issuetype().iconUrl() != null) {
            issueTypeIconUrl.set(task.fields().issuetype().iconUrl());
            hasIssueTypeIcon.set(true);
        } else {
            hasIssueTypeIcon.set(false);
            setFallbackPriorityIcon();
        }

        isCompleted.set(isTaskCompleted());
        isInProgress.set(isTaskInProgress());

        Double sp = task.fields().storyPoints();
        if (sp != null) {
            storyPoints.set(sp + " SP");
            hasStoryPoints.set(true);
        } else {
            hasStoryPoints.set(false);
        }

        Long estSeconds = task.fields().timetracking() != null ? task.fields().timetracking().originalEstimateSeconds() : null;
        Long spentSeconds = task.fields().timetracking() != null ? task.fields().timetracking().timeSpentSeconds() : null;

        this.currentEstimateSeconds = estSeconds != null ? estSeconds : 0;
        this.currentTimeSpentSeconds = spentSeconds != null ? spentSeconds : 0;

        updateProgressProperties();
    }

    public void addTimeSpent(long seconds) {
        this.currentTimeSpentSeconds += seconds;
        updateProgressProperties();
    }

    private void updateProgressProperties() {
        if (currentEstimateSeconds > 0) {
            double pct = Math.min(1.0, (double) currentTimeSpentSeconds / currentEstimateSeconds);
            progress.set(pct);
            progressPercentage.set((int) (pct * 100) + "%");
            hasProgress.set(true);
        } else {
            hasProgress.set(false);
            progress.set(0.0);
        }
    }

    private void setFallbackPriorityIcon() {
        priorityIconLiteral.set("fltfmz-warning-20");
        if (task.fields() != null && task.fields().priority() != null) {
            String pName = task.fields().priority().name();
            if (pName != null) {
                if (pName.equalsIgnoreCase("High") || pName.equalsIgnoreCase("Highest")) {
                    priorityIconColor.set("#d7383b");
                } else if (pName.equalsIgnoreCase("Medium")) {
                    priorityIconColor.set("#ff8f70");
                } else {
                    priorityIconColor.set("#8A8A8A");
                    priorityIconLiteral.set("fltfal-arrow-down-20");
                }
            }
        }
    }

    private boolean isTaskCompleted() {
        if (task.fields() != null && task.fields().status() != null && task.fields().status().statusCategory() != null) {
            return "done".equalsIgnoreCase(task.fields().status().statusCategory().key());
        }
        return false;
    }

    private boolean isTaskInProgress() {
        if (task.fields() != null && task.fields().status() != null && task.fields().status().statusCategory() != null) {
            return "indeterminate".equalsIgnoreCase(task.fields().status().statusCategory().key());
        }
        return false;
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        } else {
            return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
        }
    }

    public JiraTask getTask() { return task; }

    public StringProperty titleProperty() { return title; }
    public StringProperty taskKeyProperty() { return taskKey; }
    public StringProperty epicProperty() { return epic; }
    public StringProperty assigneeInitialsProperty() { return assigneeInitials; }
    public StringProperty priorityIconLiteralProperty() { return priorityIconLiteral; }
    public StringProperty priorityIconColorProperty() { return priorityIconColor; }
    public StringProperty issueTypeIconUrlProperty() { return issueTypeIconUrl; }
    public StringProperty storyPointsProperty() { return storyPoints; }
    public BooleanProperty isCompletedProperty() { return isCompleted; }
    public BooleanProperty isInProgressProperty() { return isInProgress; }
    public DoubleProperty progressProperty() { return progress; }
    public StringProperty progressPercentageProperty() { return progressPercentage; }
    public BooleanProperty hasProgressProperty() { return hasProgress; }
    public BooleanProperty hasStoryPointsProperty() { return hasStoryPoints; }
    public BooleanProperty hasEpicProperty() { return hasEpic; }
    public BooleanProperty hasAssigneeProperty() { return hasAssignee; }
    public BooleanProperty hasIssueTypeIconProperty() { return hasIssueTypeIcon; }
}
