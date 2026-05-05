package com.signongroup.focus.view.jira;

import com.signongroup.focus.view.WindowManager;
import com.signongroup.focus.viewmodel.BoardViewModel;
import com.signongroup.focus.viewmodel.JiraBoardViewModel;
import com.signongroup.focus.viewmodel.FocusViewModel;
import com.signongroup.focus.viewmodel.TaskCardViewModel;
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
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
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
    private final FocusViewModel mainViewModel;

    @FXML private ComboBox<BoardViewModel> boardComboBox;
    @FXML private MenuButton filterMenuButton;
    @FXML private VBox columnsContainer;
    @FXML private StackPane createIssueOverlay;

    private final Map<String, VBox> columnListMap = new HashMap<>();

    @Inject
    public JiraBoardViewController(JiraBoardViewModel viewModel, WindowManager windowManager, FocusViewModel mainViewModel) {
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
        viewModel.getDynamicColumnNames().addListener((ListChangeListener.Change<? extends String> c) -> {
            Platform.runLater(this::rebuildColumnsUI);
        });

        // If data is already cached (e.g. returning to the view), render it immediately
        if (!viewModel.getDynamicColumnNames().isEmpty()) {
            Platform.runLater(this::rebuildColumnsUI);
        }

        // Listen for task changes in all columns
        viewModel.getColumnTasksMap().addListener((javafx.collections.MapChangeListener.Change<? extends String, ? extends javafx.collections.ObservableList<TaskCardViewModel>> change) -> {
            if (change.wasAdded()) {
                String colName = change.getKey();
                change.getValueAdded().addListener((ListChangeListener.Change<? extends TaskCardViewModel> c) -> {
                     updateTaskListUI(colName, change.getValueAdded());
                });
            }
        });

        // Attach listeners to any existing columns immediately (to handle cached state)
        for (Map.Entry<String, javafx.collections.ObservableList<TaskCardViewModel>> entry : viewModel.getColumnTasksMap().entrySet()) {
            String colName = entry.getKey();
            entry.getValue().addListener((ListChangeListener.Change<? extends TaskCardViewModel> c) -> {
                updateTaskListUI(colName, entry.getValue());
            });
            // Initial render of cached tasks
            updateTaskListUI(colName, entry.getValue());
        }

        // Bind Create Issue Modal visibility
        if (createIssueOverlay != null) {
            createIssueOverlay.visibleProperty().bind(viewModel.isCreateModalVisibleProperty());
            createIssueOverlay.managedProperty().bind(viewModel.isCreateModalVisibleProperty());
        }

        // Fetch latest state in the background if a board is selected
        if (viewModel.getSelectedBoard() != null) {
            viewModel.refreshTasks();
        }
    }

    private void setupBoardComboBox() {
        boardComboBox.setItems(viewModel.getBoards());
        boardComboBox.valueProperty().bindBidirectional(viewModel.selectedBoardProperty());

        boardComboBox.setCellFactory(listView -> new ListCell<BoardViewModel>() {
            @Override
            protected void updateItem(BoardViewModel item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.name());
                }
            }
        });
        boardComboBox.setButtonCell(new ListCell<BoardViewModel>() {
             @Override
            protected void updateItem(BoardViewModel item, boolean empty) {
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

        for (String colName : viewModel.getDynamicColumnNames()) {
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
            dot.setStyle("-fx-fill: " + viewModel.getColorForColumn(colName) + ";");

            Label title = new Label(colName.toUpperCase());
            title.setStyle("-fx-font-family: 'Manrope'; -fx-font-weight: bold; -fx-text-fill: " + viewModel.getColorForColumn(colName) + "; -fx-font-size: 12px; -fx-text-transform: uppercase;");

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
                    viewModel.handleTaskDrop(taskKey, colName);
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

    private void updateTaskListUI(String colName, Iterable<? extends TaskCardViewModel> tasks) {
        VBox container = columnListMap.get(colName);
        if (container == null) return;

        Platform.runLater(() -> {
            container.getChildren().clear();
            for (TaskCardViewModel taskVM : tasks) {
                TaskCardComponent card = new TaskCardComponent(taskVM, windowManager, mainViewModel, viewModel);
                container.getChildren().add(card);
            }
        });
    }

    @FXML
    private void handleNewTask() {
        log.info("New Task button clicked. Opening dialog.");
        viewModel.isCreateModalVisibleProperty().set(true);
    }

    @FXML
    private void handleBackToMain() {
        windowManager.showActiveTimerView();
    }
}
