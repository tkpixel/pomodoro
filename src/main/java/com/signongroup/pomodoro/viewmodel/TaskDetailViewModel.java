package com.signongroup.pomodoro.viewmodel;

import com.signongroup.pomodoro.model.jira.JiraChangelogItem;
import com.signongroup.pomodoro.model.jira.JiraComment;
import com.signongroup.pomodoro.model.jira.JiraTask;
import com.signongroup.pomodoro.service.JiraBoardService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Auto-generated javadoc. */
@Singleton
public class TaskDetailViewModel {
  private static final Logger log = LoggerFactory.getLogger(TaskDetailViewModel.class);

  /** Auto-generated javadoc. */
  public enum TabMode {
    DETAILS,
    COMMENTS,
    HISTORY
  }

  private final JiraBoardService jiraBoardService;

  private final ObjectProperty<JiraTask> currentTask = new SimpleObjectProperty<>();
  private final ObservableList<JiraComment> comments = FXCollections.observableArrayList();
  private final ObservableList<JiraChangelogItem> history = FXCollections.observableArrayList();
  private final StringProperty newCommentText = new SimpleStringProperty("");
  private final ObjectProperty<TabMode> activeTab = new SimpleObjectProperty<>(TabMode.DETAILS);
  private final BooleanProperty isVisible = new SimpleBooleanProperty(false);
  private final BooleanProperty isLoading = new SimpleBooleanProperty(false);

  @Inject
  public TaskDetailViewModel(JiraBoardService jiraBoardService) {
    this.jiraBoardService = jiraBoardService;
  }

  public void loadTaskDetails(String taskKey) {
    isLoading.set(true);
    isVisible.set(true);
    activeTab.set(TabMode.DETAILS);
    comments.clear();
    history.clear();

    CompletableFuture<JiraTask> taskFuture = jiraBoardService.fetchIssueDetails(taskKey);
    CompletableFuture<java.util.List<JiraComment>> commentsFuture =
        jiraBoardService.fetchIssueComments(taskKey);
    CompletableFuture<java.util.List<JiraChangelogItem>> historyFuture =
        jiraBoardService.fetchIssueChangelog(taskKey);

    CompletableFuture.allOf(taskFuture, commentsFuture, historyFuture)
        .thenRun(
            () -> {
              Platform.runLater(
                  () -> {
                    currentTask.set(taskFuture.join());
                    comments.setAll(commentsFuture.join());
                    history.setAll(historyFuture.join());
                    isLoading.set(false);
                  });
            })
        .exceptionally(
            ex -> {
              log.error("Failed to load task details for " + taskKey, ex);
              Platform.runLater(() -> isLoading.set(false));
              return null;
            });
  }

  public void postComment() {
    if (newCommentText.get().trim().isEmpty() || currentTask.get() == null) return;

    String text = newCommentText.get();
    String taskKey = currentTask.get().key();
    isLoading.set(true);

    jiraBoardService
        .addComment(taskKey, text)
        .thenRun(
            () -> {
              Platform.runLater(() -> newCommentText.set(""));
              jiraBoardService
                  .fetchIssueComments(taskKey)
                  .thenAccept(
                      newComments -> {
                        Platform.runLater(
                            () -> {
                              comments.setAll(newComments);
                              isLoading.set(false);
                            });
                      });
            })
        .exceptionally(
            ex -> {
              log.error("Failed to post comment", ex);
              Platform.runLater(() -> isLoading.set(false));
              return null;
            });
  }

  public void close() {
    isVisible.set(false);
    currentTask.set(null);
    comments.clear();
    history.clear();
  }

  public ObjectProperty<JiraTask> currentTaskProperty() {
    return currentTask;
  }

  public ObservableList<JiraComment> getComments() {
    return comments;
  }

  public ObservableList<JiraChangelogItem> getHistory() {
    return history;
  }

  public StringProperty newCommentTextProperty() {
    return newCommentText;
  }

  public ObjectProperty<TabMode> activeTabProperty() {
    return activeTab;
  }

  public BooleanProperty isVisibleProperty() {
    return isVisible;
  }

  public BooleanProperty isLoadingProperty() {
    return isLoading;
  }
}
