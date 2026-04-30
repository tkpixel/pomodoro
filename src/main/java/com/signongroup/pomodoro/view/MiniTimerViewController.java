package com.signongroup.pomodoro.view;

import com.signongroup.pomodoro.service.ActiveTaskService;
import com.signongroup.pomodoro.service.TrackingService;
import com.signongroup.pomodoro.viewmodel.TaskCardViewModel;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.kordamp.ikonli.javafx.FontIcon;

@Prototype
public class MiniTimerViewController implements Initializable {

  @FXML private Label timerLabel;

  @FXML private Label taskLabel;

  @FXML private FontIcon playIcon;

  private final TrackingService trackingService;
  private final ActiveTaskService activeTaskService;
  private final WindowManager windowManager;

  @Inject
  public MiniTimerViewController(
      TrackingService trackingService,
      ActiveTaskService activeTaskService,
      WindowManager windowManager) {
    this.trackingService = trackingService;
    this.activeTaskService = activeTaskService;
    this.windowManager = windowManager;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    // Bind timer text dynamically to TrackingService
    timerLabel.textProperty().bind(trackingService.activeTimeProperty());

    // Update play/pause icon on state change from TrackingService
    trackingService
        .isRunningProperty()
        .addListener((obs, oldVal, newVal) -> updatePlayPauseIcon(newVal));
    updatePlayPauseIcon(trackingService.isRunningProperty().get());

    // Bind active task text
    activeTaskService
        .activeTaskProperty()
        .addListener((obs, oldVal, newVal) -> updateTaskText(newVal));
    updateTaskText(activeTaskService.getActiveTask());
  }

  private void updatePlayPauseIcon(boolean isRunning) {
    if (isRunning) {
      playIcon.setIconLiteral("fltfmz-pause-20");
    } else {
      playIcon.setIconLiteral("fltfmz-play-20");
    }
  }

  private void updateTaskText(TaskCardViewModel activeTask) {
    if (activeTask != null) {
      taskLabel.setText(
          (activeTask.taskKeyProperty().get() + ": " + activeTask.titleProperty().get())
              .toUpperCase());
    } else {
      taskLabel.setText("NO ACTIVE TASK");
    }
  }

  @FXML
  public void handleMaximize(ActionEvent event) {
    windowManager.toggleMiniMode(false);
  }

  @FXML
  public void handlePlayPause(ActionEvent event) {
    trackingService.toggleTimer();
  }

  @FXML
  public void handleStop(ActionEvent event) {
    trackingService.resetTimer();
  }
}
