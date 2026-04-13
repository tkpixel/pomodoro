package com.signongroup.pomodoro.view;

import com.signongroup.pomodoro.viewmodel.SettingsViewModel;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

@Prototype
public class SettingsOverlayViewController implements Initializable {

    @FXML
    private StackPane overlayRoot;

    @FXML
    private Region backgroundRegion;

    @FXML
    private VBox bottomSheet;

    @FXML
    private Label focusSessionLabel;
    @FXML
    private Label shortBreakLabel;
    @FXML
    private Label longBreakLabel;
    @FXML
    private Label maxSessionsLabel;

    private final SettingsViewModel settingsViewModel;

    @Inject
    public SettingsOverlayViewController(SettingsViewModel settingsViewModel) {
        this.settingsViewModel = settingsViewModel;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Bind UI to ViewModel
        focusSessionLabel.textProperty().bind(settingsViewModel.focusSessionMinutesProperty().asString("%02d"));
        shortBreakLabel.textProperty().bind(settingsViewModel.shortBreakMinutesProperty().asString("%02d"));
        longBreakLabel.textProperty().bind(settingsViewModel.longBreakMinutesProperty().asString("%02d"));
        maxSessionsLabel.textProperty().bind(settingsViewModel.maxSessionCountProperty().asString());

        // Ensure starting state is hidden
        overlayRoot.setVisible(false);
        bottomSheet.setTranslateY(1000);
    }

    public void open() {
        overlayRoot.setVisible(true);
        TranslateTransition openTransition = new TranslateTransition(Duration.millis(300), bottomSheet);
        openTransition.setToY(0);
        openTransition.setInterpolator(Interpolator.EASE_OUT);
        openTransition.play();
    }

    @FXML
    public void handleClose() {
        TranslateTransition closeTransition = new TranslateTransition(Duration.millis(300), bottomSheet);
        closeTransition.setToY(1000);
        closeTransition.setInterpolator(Interpolator.EASE_IN);
        closeTransition.setOnFinished(e -> overlayRoot.setVisible(false));
        closeTransition.play();
    }

    @FXML
    public void handleCardHover(MouseEvent event) {
        VBox card = (VBox) event.getSource();
        if (!card.getStyleClass().contains("settings-card-active")) {
            card.getStyleClass().add("settings-card-active");
            Label label = (Label) card.getChildren().get(0);
            label.getStyleClass().remove("settings-card-label-muted");
            label.getStyleClass().add("settings-card-label");
        }
    }

    @FXML
    public void handleCardExit(MouseEvent event) {
        VBox card = (VBox) event.getSource();
        card.getStyleClass().remove("settings-card-active");
        Label label = (Label) card.getChildren().get(0);
        label.getStyleClass().remove("settings-card-label");
        label.getStyleClass().add("settings-card-label-muted");
    }

    // --- Button Actions ---

    @FXML
    public void handleIncFocus() {
        settingsViewModel.incrementFocusSession();
    }

    @FXML
    public void handleDecFocus() {
        settingsViewModel.decrementFocusSession();
    }

    @FXML
    public void handleIncShortBreak() {
        settingsViewModel.incrementShortBreak();
    }

    @FXML
    public void handleDecShortBreak() {
        settingsViewModel.decrementShortBreak();
    }

    @FXML
    public void handleIncLongBreak() {
        settingsViewModel.incrementLongBreak();
    }

    @FXML
    public void handleDecLongBreak() {
        settingsViewModel.decrementLongBreak();
    }

    @FXML
    public void handleIncMaxSessions() {
        settingsViewModel.incrementMaxSessionCount();
    }

    @FXML
    public void handleDecMaxSessions() {
        settingsViewModel.decrementMaxSessionCount();
    }
}
