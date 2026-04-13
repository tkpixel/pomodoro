package com.signongroup.pomodoro.view.jira;

import com.signongroup.pomodoro.model.jira.BoardColumn;
import com.signongroup.pomodoro.model.jira.JiraBoard;
import com.signongroup.pomodoro.model.jira.JiraTask;
import com.signongroup.pomodoro.view.WindowManager;
import com.signongroup.pomodoro.viewmodel.JiraBoardViewModel;
import com.signongroup.pomodoro.viewmodel.MainViewModel;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

@Prototype
public class JiraBoardViewController implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(JiraBoardViewController.class);

    private final JiraBoardViewModel viewModel;
    private final WindowManager windowManager;
    private final MainViewModel mainViewModel;

    @FXML private ComboBox<JiraBoard> boardComboBox;
    @FXML private MenuButton filterMenuButton;
    @FXML private VBox columnsContainer;

    private final Map<String, VBox> columnListMap = new HashMap<>();

    @Inject
    public JiraBoardViewController(JiraBoardViewModel viewModel, WindowManager windowManager, MainViewModel mainViewModel) {
        this.viewModel = viewModel;
        this.windowManager = windowManager;
        this.mainViewModel = mainViewModel;
    }

    public JiraBoardViewModel getViewModel() {
        return viewModel;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        viewModel.init();

        setupBoardComboBox();

        // Listen for dynamic column changes
        viewModel.getDynamicColumns().addListener((ListChangeListener.Change<? extends BoardColumn> c) -> {
            Platform.runLater(this::rebuildColumnsUI);
        });

        // Listen for task changes in all columns
        viewModel.getColumnTasksMap().addListener((javafx.collections.MapChangeListener.Change<? extends String, ? extends javafx.collections.ObservableList<JiraTask>> change) -> {
            if (change.wasAdded()) {
                String colName = change.getKey();
                change.getValueAdded().addListener((ListChangeListener.Change<? extends JiraTask> c) -> {
                     updateTaskListUI(colName, change.getValueAdded());
                });
            }
        });
    }

    private void setupBoardComboBox() {
        boardComboBox.setItems(viewModel.getBoards());
        boardComboBox.valueProperty().bindBidirectional(viewModel.selectedBoardProperty());

        boardComboBox.setCellFactory(listView -> new ListCell<JiraBoard>() {
            @Override
            protected void updateItem(JiraBoard item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.name());
                }
            }
        });
        boardComboBox.setButtonCell(new ListCell<JiraBoard>() {
             @Override
            protected void updateItem(JiraBoard item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Select a Board...");
                } else {
                    setText(item.name());
                }
            }
        });
    }

    private void rebuildColumnsUI() {
        columnsContainer.getChildren().clear();
        filterMenuButton.getItems().clear();
        columnListMap.clear();

        for (BoardColumn column : viewModel.getDynamicColumns()) {
            String colName = column.name();

            // 1. Build Filter Menu Item
            CheckMenuItem menuItem = new CheckMenuItem(colName);
            menuItem.selectedProperty().bindBidirectional(viewModel.getColumnVisibilityProperty(colName));
            filterMenuButton.getItems().add(menuItem);

            // 2. Build Column Container (VBox)
            VBox columnVBox = new VBox();
            columnVBox.setSpacing(20);
            columnVBox.setMaxWidth(700);
            columnVBox.managedProperty().bind(viewModel.getColumnVisibilityProperty(colName));
            columnVBox.visibleProperty().bind(viewModel.getColumnVisibilityProperty(colName));

            // Header
            HBox header = new HBox();
            header.setAlignment(Pos.CENTER_LEFT);
            header.setSpacing(12);

            Circle dot = new Circle(4);
            dot.setStyle("-fx-fill: " + getColorForColumn(colName) + ";");

            Label title = new Label(colName.toUpperCase());
            title.setStyle("-fx-font-family: 'Manrope'; -fx-font-weight: bold; -fx-text-fill: " + getColorForColumn(colName) + "; -fx-font-size: 12px; -fx-text-transform: uppercase;");

            Label countBadge = new Label("0");
            countBadge.setStyle("-fx-background-color: -fx-surface-container-highest; -fx-text-fill: -fx-on-surface-variant; -fx-font-size: 10px; -fx-padding: 2 6; -fx-background-radius: 10;");

            // Bind count
            if (viewModel.getColumnTasksMap().containsKey(colName)) {
                 countBadge.textProperty().bind(Bindings.size(viewModel.getColumnTasksMap().get(colName)).asString());
            }

            header.getChildren().addAll(dot, title, countBadge);

            // Task List Container
            VBox taskListVBox = new VBox();
            taskListVBox.setSpacing(16);

            // Setup Drag & Drop Target
            taskListVBox.setOnDragOver(event -> {
                if (event.getGestureSource() != taskListVBox && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });

            taskListVBox.setOnDragDropped(event -> {
                boolean success = false;
                if (event.getDragboard().hasString()) {
                    String taskKey = event.getDragboard().getString();
                    handleTaskDrop(taskKey, colName);
                    success = true;
                }
                event.setDropCompleted(success);
                event.consume();
            });

            columnListMap.put(colName, taskListVBox);

            columnVBox.getChildren().addAll(header, taskListVBox);
            columnsContainer.getChildren().add(columnVBox);

            // Initial populate
            if (viewModel.getColumnTasksMap().containsKey(colName)) {
                updateTaskListUI(colName, viewModel.getColumnTasksMap().get(colName));
            }
        }
    }

    private String getColorForColumn(String colName) {
        String lower = colName.toLowerCase();
        if (lower.contains("progress") || lower.contains("doing")) {
            return "-fx-primary";
        } else if (lower.contains("done") || lower.contains("completed") || lower.contains("closed")) {
            return "#22c55e"; // Tailwind green
        } else {
            return "#777575"; // Outline variant
        }
    }

    private void updateTaskListUI(String colName, Iterable<? extends JiraTask> tasks) {
        VBox container = columnListMap.get(colName);
        if (container == null) return;

        Platform.runLater(() -> {
            container.getChildren().clear();
            for (JiraTask task : tasks) {
                TaskCardComponent card = new TaskCardComponent(task, windowManager, mainViewModel);
                container.getChildren().add(card);
            }
        });
    }

    private void handleTaskDrop(String taskKey, String targetColumnName) {
        // Find the task object from the map
        JiraTask draggedTask = null;
        for (var list : viewModel.getColumnTasksMap().values()) {
            for (JiraTask t : list) {
                if (t.key().equals(taskKey)) {
                    draggedTask = t;
                    break;
                }
            }
            if (draggedTask != null) break;
        }

        if (draggedTask != null) {
            log.info("Moving task {} to {}", taskKey, targetColumnName);
            viewModel.moveTask(draggedTask, targetColumnName);
        }
    }

    @FXML
    private void handleNewTask() {
        log.info("New Task button clicked. Stub for dialog.");
        // Implement dialog stub here if required
    }

    @FXML
    private void handleBackToMain() {
        windowManager.showMainView();
    }
}
