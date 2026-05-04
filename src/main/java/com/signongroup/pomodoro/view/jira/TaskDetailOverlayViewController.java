package com.signongroup.pomodoro.view.jira;

import com.signongroup.pomodoro.model.jira.JiraChangelogItem;
import com.signongroup.pomodoro.model.jira.JiraComment;
import com.signongroup.pomodoro.model.jira.JiraTask;
import com.signongroup.pomodoro.viewmodel.TaskDetailViewModel;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import org.kordamp.ikonli.javafx.FontIcon;

/** Auto-generated javadoc. */
@Prototype
public class TaskDetailOverlayViewController implements Initializable {

  private final TaskDetailViewModel viewModel;

  @FXML private StackPane overlayRoot;
  @FXML private Label taskTitleLabel;
  @FXML private Label statusLabel;
  @FXML private Label dueDateLabel;
  @FXML private Label assigneeLabel;

  @FXML private Button tabDetails;
  @FXML private Button tabComments;
  @FXML private Button tabHistory;

  @FXML private VBox detailsContainer;
  @FXML private VBox commentsContainer;
  @FXML private VBox historyContainer;

  @FXML private WebView descriptionWebView;
  @FXML private HBox commentFooter;
  @FXML private TextField commentInput;

  @Inject
  public TaskDetailOverlayViewController(TaskDetailViewModel viewModel) {
    this.viewModel = viewModel;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    overlayRoot.visibleProperty().bind(viewModel.isVisibleProperty());
    overlayRoot.managedProperty().bind(viewModel.isVisibleProperty());

    commentInput.textProperty().bindBidirectional(viewModel.newCommentTextProperty());

    viewModel
        .currentTaskProperty()
        .addListener(
            (obs, oldTask, newTask) -> {
              if (newTask != null) {
                updateMeta(newTask);
                updateDetails(newTask);
              }
            });

    viewModel.activeTabProperty().addListener((obs, oldTab, newTab) -> updateTabSelection(newTab));

    viewModel
        .getComments()
        .addListener(
            (javafx.collections.ListChangeListener.Change<? extends JiraComment> c) -> {
              Platform.runLater(this::renderComments);
            });

    viewModel
        .getHistory()
        .addListener(
            (javafx.collections.ListChangeListener.Change<? extends JiraChangelogItem> c) -> {
              Platform.runLater(this::renderHistory);
            });

    updateTabSelection(TaskDetailViewModel.TabMode.DETAILS);
  }

  private void updateMeta(JiraTask task) {
    taskTitleLabel.setText(task.fields().summary() != null ? task.fields().summary() : "");
    statusLabel.setText(task.fields().status() != null ? task.fields().status().name() : "");
    dueDateLabel.setText(
        task.fields().duedate() != null ? "Due " + task.fields().duedate() : "No due date");
    assigneeLabel.setText(
        task.fields().assignee() != null
            ? "Assigned to: " + task.fields().assignee().displayName()
            : "Unassigned");
  }

  private void updateDetails(JiraTask task) {
    String html = "";
    if (task.renderedFields() != null && task.renderedFields().get("description") != null) {
      html = task.renderedFields().get("description").toString();
    }

    // Wrap in dark theme styling
    String wrappedHtml =
        "<html><head><style>"
            + "body { background-color: #000000; color: #ffffff; font-family: 'Inter', sans-serif; font-size: 14px; line-height: 1.6; padding: 0; margin: 0; }"
            + "a { color: #ff8f70; }"
            + "</style></head><body>"
            + html
            + "</body></html>";

    descriptionWebView.getEngine().loadContent(wrappedHtml);
  }

  private void renderComments() {
    commentsContainer.getChildren().clear();
    tabComments.setText("Comments (" + viewModel.getComments().size() + ")");

    for (JiraComment comment : viewModel.getComments()) {
      HBox row = new HBox(16);

      StackPane avatar = new StackPane();
      avatar.setPrefSize(40, 40);
      avatar.setStyle("-fx-background-color: -fx-surface-variant; -fx-background-radius: 20;");
      FontIcon pIcon = new FontIcon("fltfmz-person-24");
      pIcon.setIconSize(20);
      pIcon.setIconColor(javafx.scene.paint.Color.web("#adaaaa"));
      avatar.getChildren().add(pIcon);

      VBox contentBox = new VBox(4);
      HBox meta = new HBox(8);
      meta.setAlignment(Pos.CENTER_LEFT);
      Label authorLabel =
          new Label(comment.author() != null ? comment.author().displayName() : "Unknown");
      authorLabel.setStyle(
          "-fx-font-weight: bold; -fx-text-fill: -fx-on-surface; -fx-font-size: 14px;");
      Label dateLabel = new Label(comment.created());
      dateLabel.setStyle("-fx-text-fill: -fx-on-surface-variant; -fx-font-size: 12px;");
      meta.getChildren().addAll(authorLabel, dateLabel);

      Label bodyLabel =
          new Label(
              comment.renderedBody() != null
                  ? comment.renderedBody().replaceAll("<[^>]*>", "")
                  : "No content");
      bodyLabel.setWrapText(true);
      bodyLabel.setStyle(
          "-fx-background-color: -fx-surface-container; -fx-padding: 16; -fx-background-radius: 0 12 12 12; -fx-text-fill: #e0e0e0; -fx-font-size: 14px;");

      contentBox.getChildren().addAll(meta, bodyLabel);
      row.getChildren().addAll(avatar, contentBox);

      commentsContainer.getChildren().add(row);
    }
  }

  private void renderHistory() {
    historyContainer.getChildren().clear();

    for (JiraChangelogItem item : viewModel.getHistory()) {
      HBox row = new HBox(16);

      StackPane icon = new StackPane();
      icon.setPrefSize(40, 40);
      icon.setStyle(
          "-fx-background-color: -fx-surface-container; -fx-background-radius: 20; -fx-border-color: -fx-surface-container-lowest; -fx-border-width: 3; -fx-border-radius: 20;");
      FontIcon hIcon = new FontIcon("fltfal-history-24");
      hIcon.setIconSize(18);
      hIcon.setIconColor(javafx.scene.paint.Color.web("#ff8f70"));
      icon.getChildren().add(hIcon);

      VBox contentBox = new VBox(4);
      contentBox.setStyle("-fx-padding: 8 0;");
      HBox meta = new HBox(8);
      meta.setAlignment(Pos.CENTER_LEFT);

      String author = item.author() != null ? item.author().displayName() : "Unknown";
      String action = "updated an issue";
      if (item.items() != null && !item.items().isEmpty()) {
        action = "updated " + item.items().get(0).field();
      }

      Label descLabel = new Label(author + " " + action);
      descLabel.setStyle(
          "-fx-font-weight: bold; -fx-text-fill: -fx-on-surface; -fx-font-size: 14px;");
      Label dateLabel = new Label(item.created());
      dateLabel.setStyle("-fx-text-fill: -fx-on-surface-variant; -fx-font-size: 12px;");
      meta.getChildren().addAll(descLabel, dateLabel);

      contentBox.getChildren().add(meta);
      row.getChildren().addAll(icon, contentBox);

      historyContainer.getChildren().add(row);
    }
  }

  private void updateTabSelection(TaskDetailViewModel.TabMode mode) {
    tabDetails.setStyle(
        "-fx-background-color: transparent; -fx-text-fill: "
            + (mode == TaskDetailViewModel.TabMode.DETAILS
                ? "-fx-primary"
                : "-fx-on-surface-variant")
            + "; -fx-font-weight: 500; -fx-font-size: 14px; -fx-padding: 0 0 16 0; -fx-border-color: "
            + (mode == TaskDetailViewModel.TabMode.DETAILS ? "-fx-primary" : "transparent")
            + " transparent transparent transparent; -fx-border-width: 0 0 2 0;");
    tabComments.setStyle(
        "-fx-background-color: transparent; -fx-text-fill: "
            + (mode == TaskDetailViewModel.TabMode.COMMENTS
                ? "-fx-primary"
                : "-fx-on-surface-variant")
            + "; -fx-font-weight: 500; -fx-font-size: 14px; -fx-padding: 0 0 16 0; -fx-border-color: "
            + (mode == TaskDetailViewModel.TabMode.COMMENTS ? "-fx-primary" : "transparent")
            + " transparent transparent transparent; -fx-border-width: 0 0 2 0;");
    tabHistory.setStyle(
        "-fx-background-color: transparent; -fx-text-fill: "
            + (mode == TaskDetailViewModel.TabMode.HISTORY
                ? "-fx-primary"
                : "-fx-on-surface-variant")
            + "; -fx-font-weight: 500; -fx-font-size: 14px; -fx-padding: 0 0 16 0; -fx-border-color: "
            + (mode == TaskDetailViewModel.TabMode.HISTORY ? "-fx-primary" : "transparent")
            + " transparent transparent transparent; -fx-border-width: 0 0 2 0;");

    detailsContainer.setVisible(mode == TaskDetailViewModel.TabMode.DETAILS);
    detailsContainer.setManaged(mode == TaskDetailViewModel.TabMode.DETAILS);
    commentsContainer.setVisible(mode == TaskDetailViewModel.TabMode.COMMENTS);
    commentsContainer.setManaged(mode == TaskDetailViewModel.TabMode.COMMENTS);
    historyContainer.setVisible(mode == TaskDetailViewModel.TabMode.HISTORY);
    historyContainer.setManaged(mode == TaskDetailViewModel.TabMode.HISTORY);

    commentFooter.setVisible(mode == TaskDetailViewModel.TabMode.COMMENTS);
    commentFooter.setManaged(mode == TaskDetailViewModel.TabMode.COMMENTS);
  }

  @FXML
  private void showDetailsTab() {
    viewModel.activeTabProperty().set(TaskDetailViewModel.TabMode.DETAILS);
  }

  @FXML
  private void showCommentsTab() {
    viewModel.activeTabProperty().set(TaskDetailViewModel.TabMode.COMMENTS);
  }

  @FXML
  private void showHistoryTab() {
    viewModel.activeTabProperty().set(TaskDetailViewModel.TabMode.HISTORY);
  }

  @FXML
  private void handleClose() {
    viewModel.close();
  }

  @FXML
  private void handlePostComment() {
    viewModel.postComment();
  }
}
