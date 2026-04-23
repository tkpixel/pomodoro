package com.signongroup.pomodoro.view.jira;

import com.signongroup.pomodoro.view.WindowManager;
import com.signongroup.pomodoro.viewmodel.JiraBoardViewModel;
import com.signongroup.pomodoro.viewmodel.PomodoroViewModel;
import com.signongroup.pomodoro.viewmodel.TaskCardViewModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
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
    @FXML private ImageView issueTypeIcon;
    @FXML private StackPane issueTypeContainer;

    @FXML private HBox storyPointsContainer;
    @FXML private Label storyPointsLabel;

    @FXML private VBox detailsContainer;
    @FXML private VBox progressContainer;
    @FXML private Label progressPercentage;
    @FXML private Region progressBarFill;

    @FXML private HBox actionContainer;
    @FXML private Button playButton;

    private final TaskCardViewModel viewModel;
    private final WindowManager windowManager;
    private final PomodoroViewModel mainViewModel;
    private final JiraBoardViewModel jiraBoardViewModel;
    private boolean isExpanded = false;

    public TaskCardComponent(TaskCardViewModel viewModel, WindowManager windowManager, PomodoroViewModel mainViewModel, JiraBoardViewModel jiraBoardViewModel) {
        this.viewModel = viewModel;
        this.windowManager = windowManager;
        this.mainViewModel = mainViewModel;
        this.jiraBoardViewModel = jiraBoardViewModel;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("TaskCardComponent.fxml"));
        fxmlLoader.setClassLoader(getClass().getClassLoader());
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            bindData();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        setupInteractivity();
    }

    private void setupInteractivity() {
        this.setOnMouseEntered(e -> {
            if (!viewModel.isCompletedProperty().get()) {
                this.setStyle("-fx-background-color: #1C1C1E; -fx-background-radius: 16; -fx-padding: 20; -fx-border-color: #494847; -fx-border-radius: 16; -fx-border-width: 1; -fx-cursor: hand;");
            }
        });
        this.setOnMouseExited(e -> {
            if (!viewModel.isCompletedProperty().get()) {
                 this.setStyle("-fx-background-color: #1C1C1E; -fx-background-radius: 16; -fx-padding: 20; -fx-border-color: transparent; -fx-border-radius: 16; -fx-border-width: 1; -fx-cursor: hand;");
            }
        });

        this.setOnMouseClicked(e -> {
            // Only toggle expansion on primary click
            if (e.getButton() == javafx.scene.input.MouseButton.PRIMARY && !e.isConsumed()) {
                isExpanded = !isExpanded;
                applyStateStyling();
            }
        });

        this.setOnDragDetected(event -> {
            Dragboard db = this.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(viewModel.taskKeyProperty().get());
            db.setContent(content);
            event.consume();
        });

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getStyleClass().add("context-menu");

        Menu moveStatusMenu = new Menu("Move Status");
        FontIcon moveIcon = new FontIcon("fltral-arrow-swap-20");
        moveIcon.setStyle("-fx-icon-color: -fx-on-surface-variant;");
        moveStatusMenu.setGraphic(moveIcon);

        MenuItem setAsActiveItem = new MenuItem("Set as Active");
        FontIcon activeIcon = new FontIcon("fltfmz-play-20");
        activeIcon.setStyle("-fx-icon-color: #FF4500;");
        setAsActiveItem.setGraphic(activeIcon);
        setAsActiveItem.setOnAction(e -> {
            if (mainViewModel != null) {
                mainViewModel.setActiveTask(viewModel);
            }
            if (windowManager != null) {
                windowManager.showActiveTimerView();
            }
        });

        SeparatorMenuItem separator = new SeparatorMenuItem();

        MenuItem assignToMeItem = new MenuItem("Assign to Me");
        FontIcon assignIcon = new FontIcon("fltrmz-person-20");
        assignIcon.setStyle("-fx-icon-color: -fx-on-surface-variant;");
        assignToMeItem.setGraphic(assignIcon);
        assignToMeItem.setOnAction(e -> {
            if (jiraBoardViewModel != null) {
                jiraBoardViewModel.assignTaskToCurrentUser(viewModel.taskKeyProperty().get());
            }
        });

        contextMenu.getItems().addAll(moveStatusMenu, setAsActiveItem, separator, assignToMeItem);

        this.setOnContextMenuRequested(e -> {
            if (jiraBoardViewModel != null) {
                moveStatusMenu.getItems().clear();

                String currentColumn = null;
                String taskKey = viewModel.taskKeyProperty().get();
                for (java.util.Map.Entry<String, javafx.collections.ObservableList<TaskCardViewModel>> entry : jiraBoardViewModel.getColumnTasksMap().entrySet()) {
                    for (TaskCardViewModel t : entry.getValue()) {
                        if (t.taskKeyProperty().get().equals(taskKey)) {
                            currentColumn = entry.getKey();
                            break;
                        }
                    }
                    if (currentColumn != null) break;
                }

                if (currentColumn != null) {
                    java.util.List<String> adjacentColumns = jiraBoardViewModel.getAdjacentColumnNames(currentColumn);
                    for (String colName : adjacentColumns) {
                        MenuItem colItem = new MenuItem(colName);
                        colItem.setOnAction(event -> jiraBoardViewModel.handleTaskDrop(taskKey, colName));
                        moveStatusMenu.getItems().add(colItem);
                    }
                }
            }

            contextMenu.show(this, e.getScreenX(), e.getScreenY());
        });

    }

    private void bindData() {
        taskTitle.textProperty().bind(viewModel.titleProperty());
        taskKey.textProperty().bind(viewModel.taskKeyProperty());

        // Epic
        epicBadge.textProperty().bind(viewModel.epicProperty());
        epicBadge.visibleProperty().bind(viewModel.hasEpicProperty());
        epicBadge.managedProperty().bind(viewModel.hasEpicProperty());

        // Assignee
        assigneeInitials.textProperty().bind(viewModel.assigneeInitialsProperty());
        avatarContainer.visibleProperty().bind(viewModel.hasAssigneeProperty());
        avatarContainer.managedProperty().bind(viewModel.hasAssigneeProperty());

        // Issue Type Icon
        viewModel.issueTypeIconUrlProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                try {
                    issueTypeIcon.setImage(new Image(newVal, true));
                } catch (Exception ignored) { }
            } else {
                issueTypeIcon.setImage(null);
            }
        });
        if (viewModel.issueTypeIconUrlProperty().get() != null && !viewModel.issueTypeIconUrlProperty().get().isEmpty()) {
            try {
                issueTypeIcon.setImage(new Image(viewModel.issueTypeIconUrlProperty().get(), true));
            } catch (Exception ignored) { }
        }

        issueTypeContainer.visibleProperty().bind(viewModel.hasIssueTypeIconProperty());
        issueTypeContainer.managedProperty().bind(viewModel.hasIssueTypeIconProperty());

        // Priority Fallback
        priorityIcon.visibleProperty().bind(viewModel.hasIssueTypeIconProperty().not());
        priorityIcon.managedProperty().bind(viewModel.hasIssueTypeIconProperty().not());

        viewModel.priorityIconLiteralProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                priorityIcon.setIconLiteral(newVal);
            }
        });
        if (viewModel.priorityIconLiteralProperty().get() != null) {
            priorityIcon.setIconLiteral(viewModel.priorityIconLiteralProperty().get());
        }

        viewModel.priorityIconColorProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                priorityIcon.setStyle("-fx-icon-color: " + newVal + ";");
            }
        });
        if (viewModel.priorityIconColorProperty().get() != null) {
            priorityIcon.setStyle("-fx-icon-color: " + viewModel.priorityIconColorProperty().get() + ";");
        }

        applyStateStyling();
    }

    private void applyStateStyling() {
        if (viewModel.isCompletedProperty().get()) {
            this.setOpacity(0.6);
            taskTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: -fx-on-surface-variant; -fx-strikethrough: true;");
            taskKey.setStyle("-fx-font-size: 10px; -fx-text-fill: -fx-on-surface-variant; -fx-strikethrough: true;");
            completedIcon.setVisible(true);
            completedIcon.setManaged(true);
            detailsContainer.setVisible(false);
            detailsContainer.setManaged(false);
            return;
        }

        // Story Points (always show if available and not expanded to progress)
        if (viewModel.hasStoryPointsProperty().get() && !isExpanded) {
            storyPointsContainer.setVisible(true);
            storyPointsContainer.setManaged(true);
            storyPointsLabel.textProperty().bind(viewModel.storyPointsProperty());
            storyPointsLabel.setGraphic(new FontIcon("fltfal-layer-20"));
        } else {
             storyPointsContainer.setVisible(false);
             storyPointsContainer.setManaged(false);
             storyPointsLabel.textProperty().unbind();
        }

        // Expansion details
        if (isExpanded) {
            detailsContainer.setVisible(true);
            detailsContainer.setManaged(true);

            if (viewModel.hasProgressProperty().get()) {
                progressContainer.setVisible(true);
                progressContainer.setManaged(true);
                actionContainer.setVisible(true);
                actionContainer.setManaged(true);

                progressPercentage.textProperty().bind(viewModel.progressPercentageProperty());

                progressBarFill.maxWidthProperty().bind(progressContainer.widthProperty().subtract(32).multiply(viewModel.progressProperty()));
                progressBarFill.minWidthProperty().bind(progressBarFill.maxWidthProperty());
                progressBarFill.prefWidthProperty().bind(progressBarFill.maxWidthProperty());
            } else {
                 progressContainer.setVisible(false);
                 progressContainer.setManaged(false);
                 actionContainer.setVisible(false);
                 actionContainer.setManaged(false);
                 progressPercentage.textProperty().unbind();
                 progressBarFill.maxWidthProperty().unbind();
            }
        } else {
            detailsContainer.setVisible(false);
            detailsContainer.setManaged(false);
            progressPercentage.textProperty().unbind();
            progressBarFill.maxWidthProperty().unbind();
        }
    }

    public TaskCardViewModel getViewModel() {
        return viewModel;
    }

    @FXML
    private void handlePlayAction() {
        if (mainViewModel != null) {
             mainViewModel.setActiveTask(viewModel);
        }
        if (windowManager != null) {
             windowManager.showActiveTimerView();
        }
    }
}
