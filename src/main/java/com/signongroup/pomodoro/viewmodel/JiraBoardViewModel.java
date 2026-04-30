package com.signongroup.pomodoro.viewmodel;

import com.signongroup.pomodoro.model.jira.BoardColumn;
import com.signongroup.pomodoro.model.jira.JiraBoard;
import com.signongroup.pomodoro.model.jira.JiraTask;
import com.signongroup.pomodoro.model.jira.JiraTransition;
import com.signongroup.pomodoro.service.JiraBoardService;
import com.signongroup.pomodoro.service.UserPreferencesService;
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
    private final UserPreferencesService userPreferencesService;

    private final ObservableList<BoardViewModel> boards = FXCollections.observableArrayList();
    private final ObjectProperty<BoardViewModel> selectedBoard = new SimpleObjectProperty<>();

    // Dynamic Columns and Tasks
    private final ObservableList<String> dynamicColumnNames = FXCollections.observableArrayList();
    private final List<BoardColumn> rawColumns = new ArrayList<>(); // Internal storage for workflow
    private final ObservableMap<String, ObservableList<TaskCardViewModel>> columnTasksMap = FXCollections.observableHashMap();
    private final ObservableMap<String, BooleanProperty> columnVisibilityMap = FXCollections.observableHashMap();

    // Internal state to hold real JiraTask models for actions
    private final java.util.Map<String, JiraTask> taskKeyToModelMap = new java.util.HashMap<>();

    private final BooleanProperty isLoading = new SimpleBooleanProperty(false);
    private final BooleanProperty isCreateModalVisible = new SimpleBooleanProperty(false);

    @Inject
    public JiraBoardViewModel(JiraBoardService jiraBoardService, UserPreferencesService userPreferencesService) {
        this.jiraBoardService = jiraBoardService;
        this.userPreferencesService = userPreferencesService;

        selectedBoard.addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                userPreferencesService.saveLastBoardId(String.valueOf(newVal.id()));
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
            List<BoardViewModel> boardVms = new ArrayList<>();
            for(JiraBoard b : fetchedBoards) {
                boardVms.add(new BoardViewModel(b.id(), b.name(), b.type(), b.location()));
            }
            boards.setAll(boardVms);

            String savedBoardIdStr = userPreferencesService.getLastBoardId();
            if (savedBoardIdStr != null && !savedBoardIdStr.isBlank()) {
                try {
                    Long savedBoardId = Long.parseLong(savedBoardIdStr);
                    Optional<BoardViewModel> boardToSelect = boards.stream()
                            .filter(b -> b.id().equals(savedBoardId))
                            .findFirst();
                    if (boardToSelect.isPresent()) {
                        selectedBoard.set(boardToSelect.get());
                    } else {
                        isLoading.set(false);
                    }
                } catch (NumberFormatException e) {
                    log.warn("Failed to parse saved board ID: {}", savedBoardIdStr);
                    isLoading.set(false);
                }
            } else {
                isLoading.set(false); // Do not auto-select if no preference is saved
            }
        })).exceptionally(ex -> {
            log.error("Failed to fetch boards", ex);
            Platform.runLater(() -> isLoading.set(false));
            return null;
        });
    }

    private void fetchBoardConfigurationAndTasks(Long boardId) {
        isLoading.set(true);

        var configFuture = jiraBoardService.fetchBoardConfiguration(boardId);
        var tasksFuture = jiraBoardService.fetchTasks(boardId);

        configFuture.thenCombine(tasksFuture, (config, tasks) -> {
            Platform.runLater(() -> {
                List<String> newColumnNames = new ArrayList<>();
                if (config.columnConfig() != null && config.columnConfig().columns() != null) {
                    for (BoardColumn col : config.columnConfig().columns()) {
                        newColumnNames.add(col.name());
                    }
                }

                if (!dynamicColumnNames.equals(newColumnNames)) {
                    java.util.Map<String, Boolean> currentVisibility = new java.util.HashMap<>();
                    columnVisibilityMap.forEach((col, prop) -> currentVisibility.put(col, prop.get()));

                    columnTasksMap.clear();
                    columnVisibilityMap.clear();
                    dynamicColumnNames.clear();
                    rawColumns.clear();

                    if (config.columnConfig() != null && config.columnConfig().columns() != null) {
                        rawColumns.addAll(config.columnConfig().columns());
                        for (BoardColumn col : config.columnConfig().columns()) {
                            columnTasksMap.put(col.name(), FXCollections.observableArrayList());
                            boolean isVisible = currentVisibility.getOrDefault(col.name(), true);
                            columnVisibilityMap.put(col.name(), new SimpleBooleanProperty(isVisible));
                            dynamicColumnNames.add(col.name());
                        }
                    }
                }

                distributeTasks(tasks);
                isLoading.set(false);
            });
            return null;
        }).exceptionally(ex -> {
            log.error("Failed to fetch board configuration or tasks for board {}", boardId, ex);
            Platform.runLater(() -> isLoading.set(false));
            return null;
        });
    }

    private void distributeTasks(List<JiraTask> tasks) {
        // Use temporary lists to batch-add tasks, preventing O(N) UI updates per list
        java.util.Map<String, List<TaskCardViewModel>> tempTaskMap = new java.util.HashMap<>();
        for (String colName : columnTasksMap.keySet()) {
            tempTaskMap.put(colName, new ArrayList<>());
        }

        taskKeyToModelMap.clear();

        for (JiraTask task : tasks) {
            taskKeyToModelMap.put(task.key(), task);
            String targetColumnName = null;
            if (task.fields() != null && task.fields().status() != null) {
                String statusId = task.fields().status().id();
                for (BoardColumn col : rawColumns) {
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
            if (targetColumnName != null && tempTaskMap.containsKey(targetColumnName)) {
                tempTaskMap.get(targetColumnName).add(new TaskCardViewModel(task));
            } else if (!rawColumns.isEmpty()) {
                // Fallback to first column
                tempTaskMap.get(rawColumns.get(0).name()).add(new TaskCardViewModel(task));
            }
        }

        // Apply batched updates to the ObservableLists
        for (java.util.Map.Entry<String, List<TaskCardViewModel>> entry : tempTaskMap.entrySet()) {
            ObservableList<TaskCardViewModel> observableList = columnTasksMap.get(entry.getKey());
            if (observableList != null) {
                observableList.setAll(entry.getValue());
            }
        }
    }

    private void clearData() {
        dynamicColumnNames.clear();
        rawColumns.clear();
        columnTasksMap.clear();
        columnVisibilityMap.clear();
        taskKeyToModelMap.clear();
    }

    public void refreshTasks() {
        if (selectedBoard.get() != null) {
            fetchBoardConfigurationAndTasks(selectedBoard.get().id());
        }
    }

    public void loadTasksForSelectedBoard() {
        refreshTasks();
    }

    public void assignTaskToCurrentUser(String taskKey) {
        isLoading.set(true);
        jiraBoardService.assignTaskToCurrentUser(taskKey).thenAccept(success -> Platform.runLater(() -> {
            if (success) {
                refreshTasks();
            } else {
                log.error("Failed to assign task.");
                isLoading.set(false);
            }
        })).exceptionally(ex -> {
            log.error("Error during task assignment: ", ex);
            Platform.runLater(() -> isLoading.set(false));
            return null;
        });
    }

    public List<String> getAdjacentColumnNames(String currentColumnName) {
        List<String> adjacent = new ArrayList<>();
        int currentIndex = dynamicColumnNames.indexOf(currentColumnName);
        if (currentIndex > 0) {
            adjacent.add(dynamicColumnNames.get(currentIndex - 1));
        }
        if (currentIndex >= 0 && currentIndex < dynamicColumnNames.size() - 1) {
            adjacent.add(dynamicColumnNames.get(currentIndex + 1));
        }
        return adjacent;
    }

    public void handleTaskDrop(String taskKey, String targetColumnName) {
        JiraTask draggedTask = taskKeyToModelMap.get(taskKey);

        if (draggedTask != null) {
            log.info("Moving task {} to {}", taskKey, targetColumnName);
            moveTask(draggedTask, targetColumnName);
        }
    }

    private void moveTask(JiraTask task, String targetColumnName) {
        // Find the target column statuses
        BoardColumn targetColumn = null;
        for (BoardColumn col : rawColumns) {
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

    public String getColorForColumn(String colName) {
        String lower = colName.toLowerCase();
        if (lower.contains("progress") || lower.contains("doing")) {
            return "-fx-primary";
        } else if (lower.contains("done") || lower.contains("completed") || lower.contains("closed")) {
            return "#22c55e"; // Tailwind green
        } else {
            return "#777575"; // Outline variant
        }
    }

    // Getters and Properties
    public ObservableList<BoardViewModel> getBoards() { return boards; }
    public ObjectProperty<BoardViewModel> selectedBoardProperty() { return selectedBoard; }
    public BoardViewModel getSelectedBoard() { return selectedBoard.get(); }
    public void setSelectedBoard(BoardViewModel board) { this.selectedBoard.set(board); }

    public ObservableList<String> getDynamicColumnNames() { return dynamicColumnNames; }
    public ObservableMap<String, ObservableList<TaskCardViewModel>> getColumnTasksMap() { return columnTasksMap; }
    public BooleanProperty getColumnVisibilityProperty(String columnName) { return columnVisibilityMap.get(columnName); }

    public BooleanProperty isLoadingProperty() { return isLoading; }
    public BooleanProperty isCreateModalVisibleProperty() { return isCreateModalVisible; }
}
