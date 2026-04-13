package com.signongroup.pomodoro.viewmodel;

import com.signongroup.pomodoro.model.jira.JiraBoard;
import com.signongroup.pomodoro.model.jira.JiraTask;
import com.signongroup.pomodoro.service.JiraBoardService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Singleton
public class JiraBoardViewModel {
    private static final Logger log = LoggerFactory.getLogger(JiraBoardViewModel.class);

    private final JiraBoardService jiraBoardService;

    private final ObservableList<JiraBoard> boards = FXCollections.observableArrayList();
    private final ObjectProperty<JiraBoard> selectedBoard = new SimpleObjectProperty<>();

    private final ObservableList<JiraTask> todoTasks = FXCollections.observableArrayList();
    private final ObservableList<JiraTask> inProgressTasks = FXCollections.observableArrayList();
    private final ObservableList<JiraTask> completedTasks = FXCollections.observableArrayList();

    private final BooleanProperty showTodo = new SimpleBooleanProperty(true);
    private final BooleanProperty showInProgress = new SimpleBooleanProperty(true);
    private final BooleanProperty showCompleted = new SimpleBooleanProperty(true);

    private final BooleanProperty isLoading = new SimpleBooleanProperty(false);

    @Inject
    public JiraBoardViewModel(JiraBoardService jiraBoardService) {
        this.jiraBoardService = jiraBoardService;

        selectedBoard.addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                fetchTasks(newVal.id());
            } else {
                clearTasks();
            }
        });
    }

    public void init() {
        fetchBoards();
    }

    private void fetchBoards() {
        isLoading.set(true);
        jiraBoardService.fetchBoards().thenAccept(fetchedBoards -> Platform.runLater(() -> {
            boards.setAll(fetchedBoards);
            if (!boards.isEmpty() && selectedBoard.get() == null) {
                selectedBoard.set(boards.get(0));
            }
            isLoading.set(false);
        })).exceptionally(ex -> {
            log.error("Failed to fetch boards", ex);
            Platform.runLater(() -> isLoading.set(false));
            return null;
        });
    }

    private void fetchTasks(Long boardId) {
        isLoading.set(true);
        jiraBoardService.fetchTasks(boardId).thenAccept(tasks -> Platform.runLater(() -> {
            distributeTasks(tasks);
            isLoading.set(false);
        })).exceptionally(ex -> {
            log.error("Failed to fetch tasks for board {}", boardId, ex);
            Platform.runLater(() -> isLoading.set(false));
            return null;
        });
    }

    private void distributeTasks(List<JiraTask> tasks) {
        clearTasks();
        for (JiraTask task : tasks) {
            if (task.fields() != null && task.fields().status() != null && task.fields().status().statusCategory() != null) {
                String category = task.fields().status().statusCategory().key();
                switch (category) {
                    case "new":
                        todoTasks.add(task);
                        break;
                    case "indeterminate":
                        inProgressTasks.add(task);
                        break;
                    case "done":
                        completedTasks.add(task);
                        break;
                    default:
                        todoTasks.add(task); // Default fallback
                        break;
                }
            } else {
                 todoTasks.add(task);
            }
        }
    }

    private void clearTasks() {
        todoTasks.clear();
        inProgressTasks.clear();
        completedTasks.clear();
    }

    public void refreshTasks() {
        if (selectedBoard.get() != null) {
            fetchTasks(selectedBoard.get().id());
        }
    }

    // Getters and Properties
    public ObservableList<JiraBoard> getBoards() { return boards; }
    public ObjectProperty<JiraBoard> selectedBoardProperty() { return selectedBoard; }
    public JiraBoard getSelectedBoard() { return selectedBoard.get(); }
    public void setSelectedBoard(JiraBoard board) { this.selectedBoard.set(board); }

    public ObservableList<JiraTask> getTodoTasks() { return todoTasks; }
    public ObservableList<JiraTask> getInProgressTasks() { return inProgressTasks; }
    public ObservableList<JiraTask> getCompletedTasks() { return completedTasks; }

    public BooleanProperty showTodoProperty() { return showTodo; }
    public boolean isShowTodo() { return showTodo.get(); }
    public void setShowTodo(boolean show) { this.showTodo.set(show); }

    public BooleanProperty showInProgressProperty() { return showInProgress; }
    public boolean isShowInProgress() { return showInProgress.get(); }
    public void setShowInProgress(boolean show) { this.showInProgress.set(show); }

    public BooleanProperty showCompletedProperty() { return showCompleted; }
    public boolean isShowCompleted() { return showCompleted.get(); }
    public void setShowCompleted(boolean show) { this.showCompleted.set(show); }

    public BooleanProperty isLoadingProperty() { return isLoading; }
}
