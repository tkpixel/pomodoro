package com.signongroup.pomodoro.view.jira;

import com.signongroup.pomodoro.model.jira.JiraTask;
import com.signongroup.pomodoro.view.WindowManager;
import com.signongroup.pomodoro.viewmodel.MainViewModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;

public class TaskCardComponent extends VBox {

    @FXML private VBox cardRoot;
    @FXML private Label epicBadge;
    @FXML private StackPane avatarContainer;
    @FXML private Label assigneeInitials;
    @FXML private Label taskTitle;
    @FXML private Label taskKey;
    @FXML private FontIcon priorityIcon;
    @FXML private FontIcon completedIcon;

    @FXML private HBox storyPointsContainer;
    @FXML private Label storyPointsLabel;

    @FXML private VBox progressContainer;
    @FXML private Label progressPercentage;
    @FXML private Region progressBarFill;

    @FXML private HBox actionContainer;
    @FXML private Button playButton;

    private JiraTask task;
    private final WindowManager windowManager;
    private final MainViewModel mainViewModel;

    public TaskCardComponent(JiraTask task, WindowManager windowManager, MainViewModel mainViewModel) {
        this.task = task;
        this.windowManager = windowManager;
        this.mainViewModel = mainViewModel;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("TaskCardComponent.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            bindData();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        setupHoverEffects();
    }

    private void setupHoverEffects() {
        this.setOnMouseEntered(e -> {
            if (!isCompleted()) {
                this.setStyle("-fx-background-color: #1C1C1E; -fx-background-radius: 16; -fx-padding: 20; -fx-border-color: #494847; -fx-border-radius: 16; -fx-border-width: 1; -fx-cursor: hand;");
            }
        });
        this.setOnMouseExited(e -> {
            if (!isCompleted()) {
                 this.setStyle("-fx-background-color: #1C1C1E; -fx-background-radius: 16; -fx-padding: 20; -fx-border-color: transparent; -fx-border-radius: 16; -fx-border-width: 1; -fx-cursor: default;");
            }
        });
    }

    private void bindData() {
        if (task == null || task.fields() == null) return;

        taskTitle.setText(task.fields().summary() != null ? task.fields().summary() : "Untitled Task");
        taskKey.setText(task.key());

        // Epic
        String epic = task.fields().epicKey();
        if (epic != null && !epic.isBlank()) {
            epicBadge.setText(epic);
            epicBadge.setVisible(true);
            epicBadge.setManaged(true);
        } else {
            epicBadge.setVisible(false);
            epicBadge.setManaged(false);
        }

        // Assignee
        if (task.fields().assignee() != null && task.fields().assignee().displayName() != null) {
            String name = task.fields().assignee().displayName();
            String initials = getInitials(name);
            assigneeInitials.setText(initials);
            // We could load avatar URL here if we had an Async Image loader
        } else {
            avatarContainer.setVisible(false);
            avatarContainer.setManaged(false);
        }

        // Priority
        if (task.fields().priority() != null) {
            String pName = task.fields().priority().name();
            priorityIcon.setIconLiteral("fltfmz-warning-20"); // fallback
            if (pName != null) {
                if (pName.equalsIgnoreCase("High") || pName.equalsIgnoreCase("Highest")) {
                    priorityIcon.setIconColor(javafx.scene.paint.Color.web("#d7383b"));
                    priorityIcon.setIconLiteral("fltfmz-warning-20");
                } else if (pName.equalsIgnoreCase("Medium")) {
                    priorityIcon.setIconColor(javafx.scene.paint.Color.web("#ff8f70"));
                    priorityIcon.setIconLiteral("fltfmz-warning-20");
                } else {
                    priorityIcon.setIconColor(javafx.scene.paint.Color.web("#8A8A8A"));
                    priorityIcon.setIconLiteral("fltfal-arrow-down-20");
                }
            }
        }

        applyStateStyling();
    }

    private boolean isCompleted() {
        if (task.fields().status() != null && task.fields().status().statusCategory() != null) {
            return "done".equalsIgnoreCase(task.fields().status().statusCategory().key());
        }
        return false;
    }

    private boolean isInProgress() {
        if (task.fields().status() != null && task.fields().status().statusCategory() != null) {
            return "indeterminate".equalsIgnoreCase(task.fields().status().statusCategory().key());
        }
        return false;
    }

    private void applyStateStyling() {
        if (isCompleted()) {
            // Grayscale & Strikethrough for completed tasks
            this.setOpacity(0.6);
            taskTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: -fx-on-surface-variant; -fx-strikethrough: true;");
            taskKey.setStyle("-fx-font-size: 10px; -fx-text-fill: -fx-on-surface-variant; -fx-strikethrough: true;");
            completedIcon.setVisible(true);
            completedIcon.setManaged(true);
            return;
        }

        Long estSeconds = task.fields().timetracking() != null ? task.fields().timetracking().originalEstimateSeconds() : null;
        Long spentSeconds = task.fields().timetracking() != null ? task.fields().timetracking().timeSpentSeconds() : null;
        Double storyPoints = task.fields().storyPoints();

        if (isInProgress() && estSeconds != null && estSeconds > 0) {
            // Show Progress Bar & Play Button
            progressContainer.setVisible(true);
            progressContainer.setManaged(true);
            actionContainer.setVisible(true);
            actionContainer.setManaged(true);

            long spent = spentSeconds != null ? spentSeconds : 0;
            double pct = Math.min(1.0, (double) spent / estSeconds);
            int pctInt = (int) (pct * 100);

            progressPercentage.setText(pctInt + "%");

            // Note: Since JavaFX Region doesn't easily support dynamic percentage width in FXML,
            // we bind its prefWidth to the container's width multiplied by the percentage.
            // For a simpler approach, we bind it to a fixed max width (e.g., 200px) or the parent.
            // Let's bind it after layout or just set a style.
            progressBarFill.maxWidthProperty().bind(progressContainer.widthProperty().subtract(32).multiply(pct));
            progressBarFill.minWidthProperty().bind(progressBarFill.maxWidthProperty());
            progressBarFill.prefWidthProperty().bind(progressBarFill.maxWidthProperty());

        } else if (!isCompleted() && storyPoints != null) {
            // Show Story points badge
            storyPointsContainer.setVisible(true);
            storyPointsContainer.setManaged(true);
            storyPointsLabel.setText(String.valueOf(storyPoints) + " SP");
            storyPointsLabel.setGraphic(new FontIcon("fltfal-layer-20"));
        }
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

    @FXML
    private void handlePlayAction() {
        if (windowManager != null && mainViewModel != null) {
             // In a real app we might set the active task in a TaskViewModel or MainViewModel
             // mainViewModel.setActiveTask(task.key() + " - " + taskTitle.getText());
             windowManager.showMainView();
        }
    }
}
