package com.signongroup.pomodoro.view;

import com.signongroup.pomodoro.viewmodel.MainViewModel;
import com.signongroup.pomodoro.viewmodel.MainViewModel.TimerState;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Arc;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller für die Hauptansicht (MVVM-Pattern).
 * Micronaut injiziert das ViewModel via @Inject.
 */
@Prototype
public class MainViewController implements Initializable {

    @FXML
    private Arc baseArc;

    @FXML
    private Arc progressArc;

    @FXML
    private Label timerLabel;

    @FXML
    private Label sessionLabel;

    @FXML
    private Label clearedTodayLabel;

    @FXML
    private Label nextBreakLabel;

    @FXML
    private FontIcon playIcon;

    @FXML
    private javafx.scene.control.Button skipButton;

    @FXML
    private HBox breakCardContainer;

    @FXML
    private StackPane breakCardIconContainer;

    @FXML
    private FontIcon breakIcon;

    @FXML
    private Label breakTitleLabel;

    @FXML
    private Region breakProgressRegion;

    @FXML
    private StatisticsOverlayViewController statisticsOverlayController;

    private final MainViewModel viewModel;
    private final WindowManager windowManager;

    @Inject
    public MainViewController(MainViewModel viewModel, WindowManager windowManager) {
        this.viewModel = viewModel;
        this.windowManager = windowManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Bind Label texts to ViewModel properties
        timerLabel.textProperty().bind(viewModel.timerTextProperty());
        sessionLabel.textProperty().bind(viewModel.sessionTextProperty());
        clearedTodayLabel.textProperty().bind(viewModel.clearedTodayTextProperty());
        nextBreakLabel.textProperty().bind(viewModel.nextBreakTextProperty());

        // Bind Arc length to progress (multiply by -360 as the Arc goes clockwise which is negative in JavaFX)
        progressArc.lengthProperty().bind(viewModel.timerProgressProperty().multiply(-360));

        // Bind break progress region width
        breakProgressRegion.prefWidthProperty().bind(breakCardContainer.widthProperty().multiply(viewModel.breakProgressProperty()));

        // Bind Skip Button visibility and managed properties
        skipButton.visibleProperty().bind(Bindings.createBooleanBinding(() ->
                viewModel.getTimerState() == TimerState.BREAK_SHORT || viewModel.getTimerState() == TimerState.BREAK_LONG, viewModel.timerStateProperty()));
        skipButton.managedProperty().bind(skipButton.visibleProperty());

        // Listeners for UI state changes
        viewModel.isRunningProperty().addListener((obs, oldVal, newVal) -> updatePlayPauseIcon(newVal));
        viewModel.timerStateProperty().addListener((obs, oldVal, newVal) -> updateUIForState(newVal));

        // Initial setup
        updatePlayPauseIcon(viewModel.getIsRunning());
        updateUIForState(viewModel.getTimerState());
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

    private void updatePlayPauseIcon(boolean isRunning) {
        if (isRunning) {
            playIcon.setIconLiteral("fltfmz-pause-20");
        } else {
            playIcon.setIconLiteral("fltfmz-play-20");
        }
        playIcon.setIconColor(javafx.scene.paint.Color.web("#000000"));
    }

    private void updateUIForState(TimerState state) {
        if (state == TimerState.BREAK_SHORT || state == TimerState.BREAK_LONG) {
            // Main timer visual changes
            if (baseArc != null) {
                baseArc.getStyleClass().add("timer-base-inactive");
            }
            progressArc.setVisible(false);

            // Break card visual changes
            breakCardContainer.getStyleClass().remove("break-active-card");
            breakCardContainer.getStyleClass().remove("break-active-card-long");
            breakCardIconContainer.getStyleClass().remove("card-icon-container-break");
            breakCardIconContainer.getStyleClass().remove("card-icon-container-break-active");
            breakCardIconContainer.getStyleClass().remove("card-icon-container-break-active-long");

            breakTitleLabel.getStyleClass().removeAll("break-title-long", "break-title-short", "break-title-default");

            if (state == TimerState.BREAK_LONG) {
                breakCardContainer.getStyleClass().add("break-active-card-long");
                breakCardIconContainer.getStyleClass().add("card-icon-container-break-active-long");
                breakTitleLabel.setText("LONG BREAK ACTIVE");
                breakTitleLabel.getStyleClass().add("break-title-long");
            } else {
                breakCardContainer.getStyleClass().add("break-active-card");
                breakCardIconContainer.getStyleClass().add("card-icon-container-break-active");
                breakTitleLabel.setText("SHORT BREAK ACTIVE");
                breakTitleLabel.getStyleClass().add("break-title-short");
            }

            breakIcon.setIconColor(javafx.scene.paint.Color.web("#000000")); // -fx-on-primary-fixed roughly
            breakTitleLabel.getStyleClass().remove("card-title");
            nextBreakLabel.getStyleClass().remove("card-value-muted");
            nextBreakLabel.getStyleClass().add("card-value");
            breakProgressRegion.setVisible(true);
        } else {
            // Main timer visual changes
            if (baseArc != null) {
                baseArc.getStyleClass().remove("timer-base-inactive");
            }
            progressArc.setVisible(true);

            // Break card visual changes
            breakCardContainer.getStyleClass().remove("break-active-card");
            breakCardContainer.getStyleClass().remove("break-active-card-long");
            breakCardIconContainer.getStyleClass().remove("card-icon-container-break-active");
            breakCardIconContainer.getStyleClass().remove("card-icon-container-break-active-long");
            if (!breakCardIconContainer.getStyleClass().contains("card-icon-container-break")) {
                breakCardIconContainer.getStyleClass().add("card-icon-container-break");
            }
            breakIcon.setIconColor(javafx.scene.paint.Color.web("#adaaaa")); // -fx-on-surface-variant roughly
            breakTitleLabel.setText("NEXT BREAK");
            breakTitleLabel.getStyleClass().removeAll("break-title-long", "break-title-short");
            breakTitleLabel.getStyleClass().add("break-title-default");
            if (!breakTitleLabel.getStyleClass().contains("card-title")) {
                breakTitleLabel.getStyleClass().add("card-title");
            }
            nextBreakLabel.getStyleClass().remove("card-value");
            if (!nextBreakLabel.getStyleClass().contains("card-value-muted")) {
                nextBreakLabel.getStyleClass().add("card-value-muted");
            }
            breakProgressRegion.setVisible(false);
        }
    }
}
