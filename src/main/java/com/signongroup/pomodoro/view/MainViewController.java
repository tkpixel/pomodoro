package com.signongroup.pomodoro.view;

import com.signongroup.pomodoro.service.TrackingService;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

@Prototype
public class MainViewController implements Initializable {

  @FXML private StackPane contentContainer;

  private final WindowManager windowManager;
  private final TrackingService trackingService;
  private final ApplicationContext context;

  @Inject
  public MainViewController(
      WindowManager windowManager, TrackingService trackingService, ApplicationContext context) {
    this.windowManager = windowManager;
    this.trackingService = trackingService;
    this.context = context;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    // React to mode changes (e.g. when StopwatchView calls windowManager.showStopwatchView())
    trackingService
        .activeModeProperty()
        .addListener((obs, oldMode, newMode) -> loadSubView(newMode));

    // Load initial sub-view based on current mode
    loadSubView(trackingService.getActiveMode());
  }

  private void loadSubView(TrackingService.TrackingMode mode) {
    String fxmlPath =
        (mode == TrackingService.TrackingMode.POMODORO)
            ? "/com/signongroup/pomodoro/view/PomodoroView.fxml"
            : "/com/signongroup/pomodoro/view/StopwatchView.fxml";
    try {
      URL resource = getClass().getResource(fxmlPath);
      FXMLLoader loader = new FXMLLoader(resource);
      loader.setClassLoader(getClass().getClassLoader());
      loader.setControllerFactory(context::getBean);
      Parent subView = loader.load();
      contentContainer.getChildren().setAll(subView);
    } catch (Exception e) {
      throw new RuntimeException("Failed to load sub-view: " + fxmlPath, e);
    }
  }

  @FXML
  public void handleMinimize(ActionEvent event) {
    windowManager.toggleMiniMode(true);
  }

  @FXML
  public void handleOpenSettingsMenu(ActionEvent event) {
    windowManager.showSettingsView();
  }
}
