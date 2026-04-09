package com.signongroup.pomodoro.view;

import com.signongroup.pomodoro.viewmodel.MainViewModel;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.shape.Arc;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller für die Hauptansicht (MVVM-Pattern).
 * Micronaut injiziert das ViewModel via @Inject.
 */
@Singleton
public class MainViewController implements Initializable {

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

    private final MainViewModel viewModel;

    @Inject
    public MainViewController(MainViewModel viewModel) {
        this.viewModel = viewModel;
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
    }
}
