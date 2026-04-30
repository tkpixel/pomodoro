package com.signongroup.pomodoro.view;

import com.signongroup.pomodoro.viewmodel.PomodoroViewModel;
import com.signongroup.pomodoro.viewmodel.PomodoroViewModel.TimerState;
import com.signongroup.pomodoro.viewmodel.TaskCardViewModel;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Controller für die Hauptansicht (MVVM-Pattern). Micronaut injiziert das ViewModel via @Inject.
 */
@Prototype
public class PomodoroViewController implements Initializable {

  private static final PseudoClass BREAK_SHORT_PSEUDO_CLASS =
      PseudoClass.getPseudoClass("break-short");
  private static final PseudoClass BREAK_LONG_PSEUDO_CLASS =
      PseudoClass.getPseudoClass("break-long");
  private static final PseudoClass ACTIVE_PSEUDO_CLASS = PseudoClass.getPseudoClass("active");

  @FXML private Group timerTickContainer;

  private final List<Line> timerTicks = new ArrayList<>();

  @FXML private Label timerLabel;

  @FXML private Label sessionLabel;

  @FXML private Label clearedTodayLabel;

  @FXML private Label nextBreakLabel;

  @FXML private FontIcon playIcon;

  @FXML private javafx.scene.control.Button skipButton;

  @FXML private HBox breakCardContainer;

  @FXML private StackPane breakCardIconContainer;

  @FXML private FontIcon breakIcon;

  @FXML private Label breakTitleLabel;

  @FXML private Region breakProgressRegion;

  @FXML private StatisticsOverlayViewController statisticsOverlayController;

  @FXML private VBox activeTaskCard;
  @FXML private Circle activeTaskIndicator;
  @FXML private Label activeTaskKeyLabel;
  @FXML private Label activeTaskTitleLabel;
  @FXML private StackPane activeTaskProgressContainer;
  @FXML private Region activeTaskProgressRegion;

  private Timeline indicatorTimeline;

  private final PomodoroViewModel viewModel;
  private final WindowManager windowManager;
  private final com.signongroup.pomodoro.viewmodel.StatisticsViewModel statisticsViewModel;
  private final com.signongroup.pomodoro.viewmodel.JiraBoardViewModel jiraBoardViewModel;

  @Inject
  public PomodoroViewController(
      PomodoroViewModel viewModel,
      WindowManager windowManager,
      com.signongroup.pomodoro.viewmodel.StatisticsViewModel statisticsViewModel,
      com.signongroup.pomodoro.viewmodel.JiraBoardViewModel jiraBoardViewModel) {
    this.viewModel = viewModel;
    this.windowManager = windowManager;
    this.statisticsViewModel = statisticsViewModel;
    this.jiraBoardViewModel = jiraBoardViewModel;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    // Bind Label texts to ViewModel properties
    timerLabel.textProperty().bind(viewModel.timerTextProperty());
    sessionLabel.textProperty().bind(viewModel.sessionTextProperty());
    clearedTodayLabel.textProperty().bind(viewModel.clearedTodayTextProperty());
    nextBreakLabel.textProperty().bind(viewModel.nextBreakTextProperty());

    // Generate 60 tick lines
    int numTicks = 60;
    double radius = 117.0;
    // In the design, ticks are arranged in a circle. We want to start from the top (12 o'clock).
    // 0 degrees in standard trig is right (3 o'clock). To start at top, subtract 90 degrees.
    for (int i = 0; i < numTicks; i++) {
      // angle in radians. Start at top: -PI/2. Then go clockwise.
      double angle = -Math.PI / 2.0 + (2.0 * Math.PI * i / numTicks);

      boolean isHighlight = (i % 5 == 0); // Highlight every 5th second
      double tickLength = isHighlight ? 16.0 : 10.0;
      double strokeWidth = isHighlight ? 5.0 : 4.0;

      // Start of line (outer edge)
      double startX = Math.cos(angle) * radius;
      double startY = Math.sin(angle) * radius;

      // End of line (inner edge)
      double endX = Math.cos(angle) * (radius - tickLength);
      double endY = Math.sin(angle) * (radius - tickLength);

      Line tick = new Line(startX, startY, endX, endY);
      tick.setStrokeWidth(strokeWidth);
      tick.getStyleClass().add("timer-tick");

      timerTicks.add(tick);
      timerTickContainer.getChildren().add(tick);
    }

    // Listener for timer text to parse seconds for ticks
    viewModel
        .timerTextProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              updateTicksActiveState(newVal, viewModel.getTimerState());
            });

    // Bind break progress region width
    breakProgressRegion.minWidthProperty().bind(breakProgressRegion.maxWidthProperty());
    breakProgressRegion.prefWidthProperty().bind(breakProgressRegion.maxWidthProperty());
    breakProgressRegion
        .maxWidthProperty()
        .bind(breakCardContainer.widthProperty().multiply(viewModel.breakProgressProperty()));

    // Bind Skip Button visibility and managed properties
    skipButton
        .visibleProperty()
        .bind(
            Bindings.createBooleanBinding(
                () ->
                    viewModel.getTimerState() == TimerState.BREAK_SHORT
                        || viewModel.getTimerState() == TimerState.BREAK_LONG,
                viewModel.timerStateProperty()));
    skipButton.managedProperty().bind(skipButton.visibleProperty());

    // Listeners for UI state changes
    viewModel.isRunningProperty().addListener((obs, oldVal, newVal) -> updatePlayPauseIcon(newVal));

    // Reactive Declarative Styling bindings
    breakProgressRegion
        .visibleProperty()
        .bind(
            Bindings.createBooleanBinding(
                () ->
                    viewModel.getTimerState() == TimerState.BREAK_SHORT
                        || viewModel.getTimerState() == TimerState.BREAK_LONG,
                viewModel.timerStateProperty()));

    breakTitleLabel
        .textProperty()
        .bind(
            Bindings.createStringBinding(
                () -> {
                  if (viewModel.getTimerState() == TimerState.BREAK_LONG)
                    return "LONG BREAK ACTIVE";
                  if (viewModel.getTimerState() == TimerState.BREAK_SHORT)
                    return "SHORT BREAK ACTIVE";
                  return "NEXT BREAK";
                },
                viewModel.timerStateProperty()));

    viewModel.timerStateProperty().addListener((obs, oldVal, newVal) -> updateUIForState(newVal));

    // Active Task Card bindings
    if (activeTaskCard != null) {
      activeTaskCard.visibleProperty().bind(viewModel.activeTaskProperty().isNotNull());
      activeTaskCard.managedProperty().bind(activeTaskCard.visibleProperty());
      activeTaskProgressRegion.minWidthProperty().bind(activeTaskProgressRegion.maxWidthProperty());
      activeTaskProgressRegion
          .prefWidthProperty()
          .bind(activeTaskProgressRegion.maxWidthProperty());

      viewModel
          .activeTaskProperty()
          .addListener(
              (obs, oldVal, newVal) -> {
                if (newVal != null) {
                  activeTaskTitleLabel.textProperty().bind(newVal.titleProperty());
                  activeTaskKeyLabel.textProperty().bind(newVal.taskKeyProperty());
                  activeTaskProgressContainer.visibleProperty().bind(newVal.hasProgressProperty());
                  activeTaskProgressContainer.managedProperty().bind(newVal.hasProgressProperty());
                  activeTaskProgressRegion
                      .maxWidthProperty()
                      .bind(
                          activeTaskProgressContainer
                              .widthProperty()
                              .multiply(newVal.progressProperty()));
                } else {
                  activeTaskTitleLabel.textProperty().unbind();
                  activeTaskKeyLabel.textProperty().unbind();
                  activeTaskProgressContainer.visibleProperty().unbind();
                  activeTaskProgressContainer.managedProperty().unbind();
                  activeTaskProgressRegion.maxWidthProperty().unbind();
                }
              });

      if (viewModel.activeTaskProperty().get() != null) {
        TaskCardViewModel newVal = viewModel.activeTaskProperty().get();
        activeTaskTitleLabel.textProperty().bind(newVal.titleProperty());
        activeTaskKeyLabel.textProperty().bind(newVal.taskKeyProperty());
        activeTaskProgressContainer.visibleProperty().bind(newVal.hasProgressProperty());
        activeTaskProgressContainer.managedProperty().bind(newVal.hasProgressProperty());
        activeTaskProgressRegion
            .maxWidthProperty()
            .bind(activeTaskProgressContainer.widthProperty().multiply(newVal.progressProperty()));
      }
    }

    // Indicator Pulse Animation
    if (activeTaskIndicator != null) {
      indicatorTimeline =
          new Timeline(
              new KeyFrame(
                  javafx.util.Duration.ZERO,
                  new KeyValue(activeTaskIndicator.opacityProperty(), 1.0)),
              new KeyFrame(
                  javafx.util.Duration.seconds(1),
                  new KeyValue(activeTaskIndicator.opacityProperty(), 0.3)),
              new KeyFrame(
                  javafx.util.Duration.seconds(2),
                  new KeyValue(activeTaskIndicator.opacityProperty(), 1.0)));
      indicatorTimeline.setCycleCount(Animation.INDEFINITE);
      indicatorTimeline.play();
    }

    // Initial setup
    updatePlayPauseIcon(viewModel.getIsRunning());
    updateUIForState(viewModel.getTimerState());
    updateTicksActiveState(viewModel.timerTextProperty().get(), viewModel.getTimerState());
  }

  private void updateTicksActiveState(String timeText, TimerState state) {
    boolean isBreak = state == TimerState.BREAK_SHORT || state == TimerState.BREAK_LONG;

    if (isBreak) {
      // Hide progress visually during break, similar to how progressArc was hidden
      for (Line tick : timerTicks) {
        tick.pseudoClassStateChanged(ACTIVE_PSEUDO_CLASS, false);
      }
    } else {
      int seconds = 0;
      try {
        String[] parts = timeText.split(":");
        if (parts.length == 2) {
          seconds = Integer.parseInt(parts[1]);
        }
      } catch (NumberFormatException ignored) {
      }

      int activeCount = (seconds == 0) ? 60 : seconds;

      for (int i = 0; i < timerTicks.size(); i++) {
        timerTicks.get(i).pseudoClassStateChanged(ACTIVE_PSEUDO_CLASS, i < activeCount);
      }
    }
  }

  @FXML
  public void handlePlayPause(ActionEvent event) {
    viewModel.toggleTimer();
  }

  @FXML
  public void handleDynamicAction(ActionEvent event) {
    viewModel.skipBreak();
  }

  @FXML
  public void handleResetPhase(ActionEvent event) {
    viewModel.resetCurrentPhase();
  }

  @FXML
  public void handleOpenJiraBoard(ActionEvent event) {
    windowManager.showJiraBoardView();
  }

  @FXML
  public void handleOpenStatistics(ActionEvent event) {
    if (statisticsOverlayController != null) {
      com.signongroup.pomodoro.viewmodel.BoardViewModel currentBoard =
          jiraBoardViewModel.getSelectedBoard();
      com.signongroup.pomodoro.model.jira.JiraBoard dummyBoard = null;
      if (currentBoard != null) {
        dummyBoard =
            new com.signongroup.pomodoro.model.jira.JiraBoard(
                currentBoard.id(),
                currentBoard.name(),
                currentBoard.type(),
                currentBoard.location());
      }
      statisticsViewModel.loadStatistics(dummyBoard);
      statisticsOverlayController.open();
    }
  }

  @FXML
  public void handleSwitchToStopwatch(ActionEvent event) {
    windowManager.showStopwatchView();
  }

  private void updatePlayPauseIcon(boolean isRunning) {
    if (isRunning) {
      playIcon.setIconLiteral("fltfmz-pause-20");
    } else {
      playIcon.setIconLiteral("fltfmz-play-20");
    }
    playIcon.setIconColor(javafx.scene.paint.Color.web("#000000"));
  }

  private void updateUIForState(TimerState state) {
    boolean isBreakShort = state == TimerState.BREAK_SHORT;
    boolean isBreakLong = state == TimerState.BREAK_LONG;
    boolean isActiveBreak = isBreakShort || isBreakLong;

    // Update ticks based on state
    updateTicksActiveState(viewModel.timerTextProperty().get(), state);

    breakCardContainer.pseudoClassStateChanged(BREAK_SHORT_PSEUDO_CLASS, isBreakShort);
    breakCardContainer.pseudoClassStateChanged(BREAK_LONG_PSEUDO_CLASS, isBreakLong);
    breakCardIconContainer.pseudoClassStateChanged(BREAK_SHORT_PSEUDO_CLASS, isBreakShort);
    breakCardIconContainer.pseudoClassStateChanged(BREAK_LONG_PSEUDO_CLASS, isBreakLong);
    breakTitleLabel.pseudoClassStateChanged(BREAK_SHORT_PSEUDO_CLASS, isBreakShort);
    breakTitleLabel.pseudoClassStateChanged(BREAK_LONG_PSEUDO_CLASS, isBreakLong);
    nextBreakLabel.pseudoClassStateChanged(ACTIVE_PSEUDO_CLASS, isActiveBreak);
  }
}
