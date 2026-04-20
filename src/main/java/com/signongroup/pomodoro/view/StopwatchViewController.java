package com.signongroup.pomodoro.view;

import com.signongroup.pomodoro.viewmodel.StopwatchViewModel;
import com.signongroup.pomodoro.viewmodel.TaskCardViewModel;
import io.micronaut.context.annotation.Prototype;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.layout.Region;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ResourceBundle;

@Prototype
public class StopwatchViewController implements Initializable {

    @FXML private Label timerLabel;
    @FXML private Label clearedTodayLabel;
    @FXML private FontIcon playIcon;

    @FXML private VBox activeTaskCard;
    @FXML private Label activeTaskTitleLabel;
    @FXML private Label activeTaskKeyLabel;
    @FXML private Circle activeTaskIndicator;
    @FXML private StackPane activeTaskProgressContainer;
    @FXML private Region activeTaskProgressRegion;
    @FXML private StatisticsOverlayViewController statisticsOverlayController;

    private final StopwatchViewModel viewModel;
    private final WindowManager windowManager;
    private Timeline indicatorTimeline;

    @jakarta.inject.Inject
    public StopwatchViewController(StopwatchViewModel viewModel, WindowManager windowManager) {
        this.viewModel = viewModel;
        this.windowManager = windowManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        timerLabel.textProperty().bind(viewModel.timerTextProperty());
        clearedTodayLabel.textProperty().bind(viewModel.clearedTodayTextProperty());

        viewModel.isRunningProperty().addListener((obs, oldVal, newVal) -> updatePlayPauseIcon(newVal));

        if (activeTaskCard != null) {
            activeTaskCard.visibleProperty().bind(viewModel.activeTaskProperty().isNotNull());
            activeTaskCard.managedProperty().bind(activeTaskCard.visibleProperty());
            activeTaskProgressRegion.minWidthProperty().bind(activeTaskProgressRegion.maxWidthProperty());
            activeTaskProgressRegion.prefWidthProperty().bind(activeTaskProgressRegion.maxWidthProperty());

            viewModel.activeTaskProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    activeTaskTitleLabel.textProperty().bind(newVal.titleProperty());
                    activeTaskKeyLabel.textProperty().bind(newVal.taskKeyProperty());
                    activeTaskProgressContainer.visibleProperty().bind(newVal.hasProgressProperty());
                    activeTaskProgressContainer.managedProperty().bind(newVal.hasProgressProperty());
                    activeTaskProgressRegion.maxWidthProperty().bind(activeTaskProgressContainer.widthProperty().multiply(newVal.progressProperty()));
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
                activeTaskProgressRegion.maxWidthProperty().bind(activeTaskProgressContainer.widthProperty().multiply(newVal.progressProperty()));
            }
        }

        if (activeTaskIndicator != null) {
            indicatorTimeline = new Timeline(
                new KeyFrame(javafx.util.Duration.ZERO, new KeyValue(activeTaskIndicator.opacityProperty(), 1.0)),
                new KeyFrame(javafx.util.Duration.seconds(1), new KeyValue(activeTaskIndicator.opacityProperty(), 0.3)),
                new KeyFrame(javafx.util.Duration.seconds(2), new KeyValue(activeTaskIndicator.opacityProperty(), 1.0))
            );
            indicatorTimeline.setCycleCount(Animation.INDEFINITE);
            indicatorTimeline.play();
        }

        updatePlayPauseIcon(viewModel.isRunningProperty().get());
    }

    private void updatePlayPauseIcon(boolean isRunning) {
        if (isRunning) {
            playIcon.setIconLiteral("fltfmz-stop-20");
        } else {
            playIcon.setIconLiteral("fltfmz-play-20");
        }
        playIcon.setIconColor(javafx.scene.paint.Color.web("#000000"));
    }

    @FXML
    public void handlePlayPause(ActionEvent event) {
        viewModel.toggleTimer();
    }

    @FXML
    public void handleResetTimer(ActionEvent event) {
        viewModel.resetTimer();
    }

    @FXML
    public void handleMinimize(ActionEvent event) {
        windowManager.toggleMiniMode(true);
    }

    @FXML
    public void handleOpenSettingsMenu(ActionEvent event) {
        windowManager.showSettingsView();
    }

    @FXML
    public void handleOpenJiraBoard(ActionEvent event) {
        windowManager.showJiraBoardView();
    }

    @FXML
    public void handleOpenStatistics(ActionEvent event) {
        if (statisticsOverlayController != null) {
            statisticsOverlayController.open();
        }
    }

    @FXML
    public void handleSwitchToPomodoro(ActionEvent event) {
        windowManager.showMainView();
    }
}
