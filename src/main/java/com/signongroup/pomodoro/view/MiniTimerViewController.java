package com.signongroup.pomodoro.view;

import com.signongroup.pomodoro.viewmodel.MainViewModel;
import com.signongroup.pomodoro.viewmodel.TaskCardViewModel;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ResourceBundle;

@Prototype
public class MiniTimerViewController implements Initializable {

    @FXML
    private Label timerLabel;

    @FXML
    private Label taskLabel;

    @FXML
    private FontIcon playIcon;

    private final MainViewModel viewModel;
    private final WindowManager windowManager;

    @Inject
    public MiniTimerViewController(MainViewModel viewModel, WindowManager windowManager) {
        this.viewModel = viewModel;
        this.windowManager = windowManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Bind timer text
        timerLabel.textProperty().bind(viewModel.timerTextProperty());

        // Update play/pause icon on state change
        viewModel.isRunningProperty().addListener((obs, oldVal, newVal) -> updatePlayPauseIcon(newVal));
        updatePlayPauseIcon(viewModel.getIsRunning());

        // Bind active task text
        viewModel.activeTaskProperty().addListener((obs, oldVal, newVal) -> updateTaskText(newVal));
        updateTaskText(viewModel.activeTaskProperty().get());
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
            taskLabel.setText((activeTask.taskKeyProperty().get() + ": " + activeTask.titleProperty().get()).toUpperCase());
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
        viewModel.toggleTimer();
    }

    @FXML
    public void handleStop(ActionEvent event) {
        viewModel.resetCurrentPhase();
    }
}
