package com.signongroup.pomodoro.viewmodel;

import com.signongroup.pomodoro.model.jira.BoardColumn;
import com.signongroup.pomodoro.model.jira.JiraBoard;
import com.signongroup.pomodoro.model.jira.JiraTask;
import com.signongroup.pomodoro.model.jira.JiraTransition;
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
import javafx.collections.ObservableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Singleton
public class JiraBoardViewModel {
    private static final Logger log = LoggerFactory.getLogger(JiraBoardViewModel.class);

    private final JiraBoardService jiraBoardService;

    private final ObservableList<JiraBoard> boards = FXCollections.observableArrayList();
    private final ObjectProperty<JiraBoard> selectedBoard = new SimpleObjectProperty<>();

    // Dynamic Columns and Tasks
    private final ObservableList<BoardColumn> dynamicColumns = FXCollections.observableArrayList();
    private final ObservableMap<String, ObservableList<JiraTask>> columnTasksMap = FXCollections.observableHashMap();
    private final ObservableMap<String, BooleanProperty> columnVisibilityMap = FXCollections.observableHashMap();

    private final BooleanProperty isLoading = new SimpleBooleanProperty(false);

    @Inject
    public JiraBoardViewModel(JiraBoardService jiraBoardService) {
        this.jiraBoardService = jiraBoardService;

        selectedBoard.addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                fetchBoardConfigurationAndTasks(newVal.id());
            } else {
                clearData();
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
            } else {
                isLoading.set(false);
            }
        })).exceptionally(ex -> {
            log.error("Failed to fetch boards", ex);
            Platform.runLater(() -> isLoading.set(false));
            return null;
        });
    }

    private void fetchBoardConfigurationAndTasks(Long boardId) {
        isLoading.set(true);
        jiraBoardService.fetchBoardConfiguration(boardId).thenCompose(config -> {
            Platform.runLater(() -> {
                dynamicColumns.clear();
                columnTasksMap.clear();
                columnVisibilityMap.clear();
                if (config.columnConfig() != null && config.columnConfig().columns() != null) {
                    for (BoardColumn col : config.columnConfig().columns()) {
                        dynamicColumns.add(col);
                        columnTasksMap.put(col.name(), FXCollections.observableArrayList());
                        columnVisibilityMap.put(col.name(), new SimpleBooleanProperty(true));
                    }
                }
            });
            return jiraBoardService.fetchTasks(boardId);
        }).thenAccept(tasks -> Platform.runLater(() -> {
            distributeTasks(tasks);
            isLoading.set(false);
        })).exceptionally(ex -> {
            log.error("Failed to fetch board configuration or tasks for board {}", boardId, ex);
            Platform.runLater(() -> isLoading.set(false));
            return null;
        });
    }

    private void distributeTasks(List<JiraTask> tasks) {
        for (ObservableList<JiraTask> list : columnTasksMap.values()) {
            list.clear();
        }
        for (JiraTask task : tasks) {
            String targetColumnName = null;
            if (task.fields() != null && task.fields().status() != null) {
                String statusId = task.fields().status().id();
                for (BoardColumn col : dynamicColumns) {
                    if (col.statuses() != null) {
                        for (BoardColumn.StatusMapping mapping : col.statuses()) {
                            if (statusId.equals(mapping.id())) {
                                targetColumnName = col.name();
                                break;
                            }
                        }
                    }
                    if (targetColumnName != null) break;
                }
            }
            if (targetColumnName != null && columnTasksMap.containsKey(targetColumnName)) {
                columnTasksMap.get(targetColumnName).add(task);
            } else if (!dynamicColumns.isEmpty()) {
                // Fallback to first column
                columnTasksMap.get(dynamicColumns.get(0).name()).add(task);
            }
        }
    }

    private void clearData() {
        dynamicColumns.clear();
        columnTasksMap.clear();
        columnVisibilityMap.clear();
    }

    public void refreshTasks() {
        if (selectedBoard.get() != null) {
            fetchBoardConfigurationAndTasks(selectedBoard.get().id());
        }
    }

    public void moveTask(JiraTask task, String targetColumnName) {
        // Find the target column statuses
        BoardColumn targetColumn = null;
        for (BoardColumn col : dynamicColumns) {
            if (col.name().equals(targetColumnName)) {
                targetColumn = col;
                break;
            }
        }

        if (targetColumn == null || targetColumn.statuses() == null || targetColumn.statuses().isEmpty()) {
            log.warn("Target column {} has no mapped statuses.", targetColumnName);
            refreshTasks(); // Reset UI
            return;
        }

        final List<String> targetStatusIds = new ArrayList<>();
        for (BoardColumn.StatusMapping sm : targetColumn.statuses()) {
            targetStatusIds.add(sm.id());
        }

        isLoading.set(true);
        jiraBoardService.fetchTransitions(task.key()).thenCompose(transitions -> {
            Optional<JiraTransition> validTransition = transitions.stream()
                .filter(t -> t.to() != null && targetStatusIds.contains(t.to().id()))
                .findFirst();

            if (validTransition.isPresent()) {
                return jiraBoardService.moveTask(task.key(), validTransition.get().id());
            } else {
                throw new RuntimeException("No valid transition found to move " + task.key() + " to " + targetColumnName);
            }
        }).thenAccept(success -> Platform.runLater(() -> {
            if (success) {
                refreshTasks();
            } else {
                log.error("Failed to move task.");
                refreshTasks();
            }
        })).exceptionally(ex -> {
            log.error("Error during task transition: ", ex);
            Platform.runLater(() -> {
                isLoading.set(false);
                refreshTasks(); // Revert UI
            });
            return null;
        });
    }

    // Getters and Properties
    public ObservableList<JiraBoard> getBoards() { return boards; }
    public ObjectProperty<JiraBoard> selectedBoardProperty() { return selectedBoard; }
    public JiraBoard getSelectedBoard() { return selectedBoard.get(); }
    public void setSelectedBoard(JiraBoard board) { this.selectedBoard.set(board); }

    public ObservableList<BoardColumn> getDynamicColumns() { return dynamicColumns; }
    public ObservableMap<String, ObservableList<JiraTask>> getColumnTasksMap() { return columnTasksMap; }
    public BooleanProperty getColumnVisibilityProperty(String columnName) { return columnVisibilityMap.get(columnName); }

    public BooleanProperty isLoadingProperty() { return isLoading; }
}
