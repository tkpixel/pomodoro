package com.signongroup.pomodoro.view;

import com.signongroup.pomodoro.viewmodel.SettingsViewModel;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

import javafx.css.PseudoClass;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ResourceBundle;

@Prototype
public class SettingsViewController implements Initializable {

    private static final PseudoClass PSEUDO_CLASS_ON = PseudoClass.getPseudoClass("on");

    // --- Window Dependencies ---
    private final SettingsViewModel viewModel;
    private final WindowManager windowManager;

    // --- Accordion UI Elements ---
    @FXML private StackPane enableSessionSoundToggleTrack;
    @FXML private Region enableSessionSoundToggleThumb;
    @FXML private StackPane enableBreakSoundToggleTrack;
    @FXML private Region enableBreakSoundToggleThumb;

    @FXML private VBox durationContent;
    @FXML private FontIcon durationIcon;
    @FXML private FontIcon durationExpandIcon;
    @FXML private VBox jiraContent;
    @FXML private FontIcon jiraIcon;
    @FXML private FontIcon jiraExpandIcon;

    // --- Duration Settings UI Elements ---
    @FXML private Label focusSessionLabel;
    @FXML private Label shortBreakLabel;
    @FXML private Label longBreakLabel;
    @FXML private Label maxSessionsLabel;
    @FXML private StackPane autoStartToggleTrack;
    @FXML private Region autoStartToggleThumb;
    @FXML private StackPane autoStartSessionsToggleTrack;
    @FXML private Region autoStartSessionsToggleThumb;

    // --- Jira Settings UI Elements ---
    @FXML private TextField urlField;
    @FXML private TextField emailField;
    @FXML private PasswordField tokenFieldMasked;
    @FXML private TextField tokenFieldVisible;
    @FXML private FontIcon visibilityIcon;
    @FXML private Label statusLabel;
    @FXML private Button connectButton;

    private boolean isPasswordVisible = false;

    @Inject
    public SettingsViewController(SettingsViewModel viewModel, WindowManager windowManager) {
        this.viewModel = viewModel;
        this.windowManager = windowManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Bind Accordion Panels
        durationContent.visibleProperty().bind(viewModel.isDurationExpandedProperty());

        durationContent.managedProperty().bind(viewModel.isDurationExpandedProperty());
        jiraContent.visibleProperty().bind(viewModel.isJiraExpandedProperty());
        jiraContent.managedProperty().bind(viewModel.isJiraExpandedProperty());

        viewModel.isDurationExpandedProperty().addListener((obs, oldVal, newVal) -> {
            updateDurationHeaderStyle(newVal);
        });
        updateDurationHeaderStyle(viewModel.isDurationExpandedProperty().get());

        viewModel.isJiraExpandedProperty().addListener((obs, oldVal, newVal) -> {
            updateJiraHeaderStyle(newVal);
        });
        updateJiraHeaderStyle(viewModel.isJiraExpandedProperty().get());

        // Bind Duration UI to ViewModel
        focusSessionLabel.textProperty().bind(viewModel.focusSessionMinutesProperty().asString("%02d:00"));
        shortBreakLabel.textProperty().bind(viewModel.shortBreakMinutesProperty().asString("%02d:00"));
        longBreakLabel.textProperty().bind(viewModel.longBreakMinutesProperty().asString("%02d:00"));
        maxSessionsLabel.textProperty().bind(viewModel.maxSessionCountProperty().asString());

        // Auto Start Sessions Toggle Binding
        viewModel.autoStartSessionsProperty().addListener((obs, oldVal, newVal) -> {
            updateAutoStartSessionsToggleUI(newVal);
        });
        updateAutoStartSessionsToggleUI(viewModel.autoStartSessionsProperty().get());

        // Auto Start Breaks Toggle Binding
        viewModel.autoStartBreaksProperty().addListener((obs, oldVal, newVal) -> {
            updateAutoStartToggleUI(newVal);
        });
        updateAutoStartToggleUI(viewModel.autoStartBreaksProperty().get());

        // Enable Session Sound Toggle Binding
        viewModel.enableSessionSoundProperty().addListener((obs, oldVal, newVal) -> {
            updateEnableSessionSoundToggleUI(newVal);
        });
        updateEnableSessionSoundToggleUI(viewModel.enableSessionSoundProperty().get());

        // Enable Break Sound Toggle Binding
        viewModel.enableBreakSoundProperty().addListener((obs, oldVal, newVal) -> {
            updateEnableBreakSoundToggleUI(newVal);
        });
        updateEnableBreakSoundToggleUI(viewModel.enableBreakSoundProperty().get());

        // Bind Jira Connection UI to ViewModel
        urlField.textProperty().bindBidirectional(viewModel.urlProperty());
        emailField.textProperty().bindBidirectional(viewModel.emailProperty());

        tokenFieldMasked.textProperty().bindBidirectional(viewModel.tokenProperty());
        tokenFieldVisible.textProperty().bindBidirectional(viewModel.tokenProperty());

        statusLabel.textProperty().bind(viewModel.statusMessageProperty());

        viewModel.isSuccessProperty().addListener((obs, oldVal, newVal) -> {
            statusLabel.getStyleClass().removeAll("status-success", "status-error");
            if (newVal) {
                statusLabel.getStyleClass().add("status-success");
            } else {
                statusLabel.getStyleClass().add("status-error");
            }
        });

        connectButton.disableProperty().bind(viewModel.canConnectProperty().not());
    }

    private void updateDurationHeaderStyle(boolean isExpanded) {
        if (isExpanded) {
            durationIcon.getStyleClass().add("active");
            durationExpandIcon.setRotate(180);
        } else {
            durationIcon.getStyleClass().remove("active");
            durationExpandIcon.setRotate(0);
        }
    }

    private void updateJiraHeaderStyle(boolean isExpanded) {
        if (isExpanded) {
            jiraIcon.getStyleClass().add("active");
            jiraExpandIcon.setRotate(180);
        } else {
            jiraIcon.getStyleClass().remove("active");
            jiraExpandIcon.setRotate(0);
        }
    }

    private void updateAutoStartSessionsToggleUI(boolean isOn) {
        autoStartSessionsToggleTrack.pseudoClassStateChanged(PSEUDO_CLASS_ON, isOn);
        autoStartSessionsToggleThumb.pseudoClassStateChanged(PSEUDO_CLASS_ON, isOn);
        if (isOn) {
            javafx.scene.layout.StackPane.setAlignment(autoStartSessionsToggleThumb, javafx.geometry.Pos.CENTER_RIGHT);
        } else {
            javafx.scene.layout.StackPane.setAlignment(autoStartSessionsToggleThumb, javafx.geometry.Pos.CENTER_LEFT);
        }
    }

    private void updateAutoStartToggleUI(boolean isOn) {
        autoStartToggleTrack.pseudoClassStateChanged(PSEUDO_CLASS_ON, isOn);
        autoStartToggleThumb.pseudoClassStateChanged(PSEUDO_CLASS_ON, isOn);
        if (isOn) {
            javafx.scene.layout.StackPane.setAlignment(autoStartToggleThumb, javafx.geometry.Pos.CENTER_RIGHT);
        } else {
            javafx.scene.layout.StackPane.setAlignment(autoStartToggleThumb, javafx.geometry.Pos.CENTER_LEFT);
        }
    }

    private void updateEnableSessionSoundToggleUI(boolean isOn) {
        enableSessionSoundToggleTrack.pseudoClassStateChanged(PSEUDO_CLASS_ON, isOn);
        enableSessionSoundToggleThumb.pseudoClassStateChanged(PSEUDO_CLASS_ON, isOn);
        if (isOn) {
            javafx.scene.layout.StackPane.setAlignment(enableSessionSoundToggleThumb, javafx.geometry.Pos.CENTER_RIGHT);
        } else {
            javafx.scene.layout.StackPane.setAlignment(enableSessionSoundToggleThumb, javafx.geometry.Pos.CENTER_LEFT);
        }
    }

    private void updateEnableBreakSoundToggleUI(boolean isOn) {
        enableBreakSoundToggleTrack.pseudoClassStateChanged(PSEUDO_CLASS_ON, isOn);
        enableBreakSoundToggleThumb.pseudoClassStateChanged(PSEUDO_CLASS_ON, isOn);
        if (isOn) {
            javafx.scene.layout.StackPane.setAlignment(enableBreakSoundToggleThumb, javafx.geometry.Pos.CENTER_RIGHT);
        } else {
            javafx.scene.layout.StackPane.setAlignment(enableBreakSoundToggleThumb, javafx.geometry.Pos.CENTER_LEFT);
        }
    }

    @FXML
    public void handleBack(ActionEvent event) {
        windowManager.showActiveTimerView();
    }

    // --- Accordion Toggle Handlers ---

    /**
     * Toggles enable session sound setting.
     * @param event the mouse event
     */
    @FXML
    public void toggleEnableSessionSound(MouseEvent event) {
        viewModel.enableSessionSoundProperty().set(!viewModel.enableSessionSoundProperty().get());
    }

    /**
     * Toggles enable break sound setting.
     * @param event the mouse event
     */
    @FXML
    public void toggleEnableBreakSound(MouseEvent event) {
        viewModel.enableBreakSoundProperty().set(!viewModel.enableBreakSoundProperty().get());
    }

    @FXML
    public void toggleDurationExpanded(MouseEvent event) {
        viewModel.toggleDurationExpanded();
    }

    @FXML
    public void toggleJiraExpanded(MouseEvent event) {
        viewModel.toggleJiraExpanded();
    }

    // --- Duration Button Handlers ---
    @FXML
    public void handleIncFocus() { viewModel.incrementFocusSession(); }

    @FXML
    public void handleDecFocus() { viewModel.decrementFocusSession(); }

    @FXML
    public void handleIncShortBreak() { viewModel.incrementShortBreak(); }

    @FXML
    public void handleDecShortBreak() { viewModel.decrementShortBreak(); }

    @FXML
    public void handleIncLongBreak() { viewModel.incrementLongBreak(); }

    @FXML
    public void handleDecLongBreak() { viewModel.decrementLongBreak(); }

    @FXML
    public void handleIncMaxSessions() { viewModel.incrementMaxSessionCount(); }

    @FXML
    public void handleDecMaxSessions() { viewModel.decrementMaxSessionCount(); }

    /**
     * Toggles auto start sessions.
     * @param event the mouse event
     */
    @FXML
    public void toggleAutoStartSessions(MouseEvent event) {
        viewModel.autoStartSessionsProperty().set(!viewModel.autoStartSessionsProperty().get());
    }

    /**
     * Toggles auto start.
     * @param event the mouse event
     */
    @FXML
    public void toggleAutoStart(MouseEvent event) {
        viewModel.autoStartBreaksProperty().set(!viewModel.autoStartBreaksProperty().get());
    }

    // --- Jira Connection Handlers ---
    @FXML
    public void handleConnect(ActionEvent event) {
        viewModel.connectAndTestJira();
    }

    @FXML
    public void toggleTokenVisibility(ActionEvent event) {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            tokenFieldMasked.setVisible(false);
            tokenFieldVisible.setVisible(true);
            visibilityIcon.setIconLiteral("fltfal-eye-hide-20");
        } else {
            tokenFieldMasked.setVisible(true);
            tokenFieldVisible.setVisible(false);
            visibilityIcon.setIconLiteral("fltfal-eye-show-20");
        }
    }
}
