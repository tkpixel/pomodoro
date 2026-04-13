package com.signongroup.pomodoro.view.jira;

import com.signongroup.pomodoro.model.jira.JiraBoard;
import com.signongroup.pomodoro.model.jira.JiraTask;
import com.signongroup.pomodoro.view.WindowManager;
import com.signongroup.pomodoro.viewmodel.JiraBoardViewModel;
import com.signongroup.pomodoro.viewmodel.MainViewModel;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

@Prototype
public class JiraBoardViewController implements Initializable {

    private final JiraBoardViewModel viewModel;
    private final WindowManager windowManager;
    private final MainViewModel mainViewModel;

    @FXML private ComboBox<JiraBoard> boardComboBox;

    @FXML private CheckMenuItem filterTodo;
    @FXML private CheckMenuItem filterInProgress;
    @FXML private CheckMenuItem filterCompleted;

    @FXML private VBox todoColumn;
    @FXML private VBox inProgressColumn;
    @FXML private VBox completedColumn;

    @FXML private Label todoCountLabel;
    @FXML private Label inProgressCountLabel;
    @FXML private Label completedCountLabel;

    @FXML private VBox todoList;
    @FXML private VBox inProgressList;
    @FXML private VBox completedList;

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
        // Initialize ViewModel Data
        viewModel.init();

        // Bind Board ComboBox
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
                    setText(null);
                } else {
                    setText(item.name());
                }
            }
        });

        // Bind Filters
        viewModel.showTodoProperty().bind(filterTodo.selectedProperty());
        viewModel.showInProgressProperty().bind(filterInProgress.selectedProperty());
        viewModel.showCompletedProperty().bind(filterCompleted.selectedProperty());

        todoColumn.managedProperty().bind(viewModel.showTodoProperty());
        todoColumn.visibleProperty().bind(viewModel.showTodoProperty());
        inProgressColumn.managedProperty().bind(viewModel.showInProgressProperty());
        inProgressColumn.visibleProperty().bind(viewModel.showInProgressProperty());
        completedColumn.managedProperty().bind(viewModel.showCompletedProperty());
        completedColumn.visibleProperty().bind(viewModel.showCompletedProperty());

        // Bind Task Lists
        todoCountLabel.textProperty().bind(Bindings.size(viewModel.getTodoTasks()).asString());
        inProgressCountLabel.textProperty().bind(Bindings.size(viewModel.getInProgressTasks()).asString());
        completedCountLabel.textProperty().bind(Bindings.size(viewModel.getCompletedTasks()).asString());

        viewModel.getTodoTasks().addListener((ListChangeListener.Change<? extends JiraTask> c) -> updateTaskList(todoList, viewModel.getTodoTasks()));
        viewModel.getInProgressTasks().addListener((ListChangeListener.Change<? extends JiraTask> c) -> updateTaskList(inProgressList, viewModel.getInProgressTasks()));
        viewModel.getCompletedTasks().addListener((ListChangeListener.Change<? extends JiraTask> c) -> updateTaskList(completedList, viewModel.getCompletedTasks()));
    }

    private void updateTaskList(VBox container, Iterable<? extends JiraTask> tasks) {
        container.getChildren().clear();
        for (JiraTask task : tasks) {
            TaskCardComponent card = new TaskCardComponent(task, windowManager, mainViewModel);
            container.getChildren().add(card);
        }
    }

    @FXML
    private void handleBackToMain() {
        windowManager.showMainView();
    }
}
