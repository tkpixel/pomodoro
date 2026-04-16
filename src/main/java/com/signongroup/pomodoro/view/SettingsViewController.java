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
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ResourceBundle;

@Prototype
public class SettingsViewController implements Initializable {

    // --- Window Dependencies ---
    private final SettingsViewModel viewModel;
    private final WindowManager windowManager;

    // --- Accordion UI Elements ---
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
    @FXML private Region autoStartToggleThumb;

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

        // Animate/Style Accordion Headers based on expanded state
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

        // Auto Start Toggle Binding
        viewModel.autoStartBreaksProperty().addListener((obs, oldVal, newVal) -> {
            updateAutoStartToggleUI(newVal);
        });
        updateAutoStartToggleUI(viewModel.autoStartBreaksProperty().get());

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
            durationIcon.setStyle("-fx-icon-color: -fx-primary;");
            durationExpandIcon.setRotate(180);
        } else {
            durationIcon.setStyle("-fx-icon-color: -fx-on-surface-variant;");
            durationExpandIcon.setRotate(0);
        }
    }

    private void updateJiraHeaderStyle(boolean isExpanded) {
        if (isExpanded) {
            jiraIcon.setStyle("-fx-icon-color: -fx-primary;");
            jiraExpandIcon.setRotate(180);
        } else {
            jiraIcon.setStyle("-fx-icon-color: -fx-on-surface-variant;");
            jiraExpandIcon.setRotate(0);
        }
    }

    private void updateAutoStartToggleUI(boolean isOn) {
        autoStartToggleThumb.getParent().getStyleClass().removeAll("toggle-track-on", "toggle-track-off");
        autoStartToggleThumb.getStyleClass().removeAll("toggle-thumb-on", "toggle-thumb-off");
        if (isOn) {
            autoStartToggleThumb.getParent().getStyleClass().add("toggle-track-on");
            autoStartToggleThumb.getStyleClass().add("toggle-thumb-on");
            // Simple alignment push to the right for JavaFX StackPane
            javafx.scene.layout.StackPane.setAlignment(autoStartToggleThumb, javafx.geometry.Pos.CENTER_RIGHT);
        } else {
            autoStartToggleThumb.getParent().getStyleClass().add("toggle-track-off");
            autoStartToggleThumb.getStyleClass().add("toggle-thumb-off");
            javafx.scene.layout.StackPane.setAlignment(autoStartToggleThumb, javafx.geometry.Pos.CENTER_LEFT);
        }
    }

    @FXML
    public void handleBack(ActionEvent event) {
        windowManager.showMainView();
    }

    // --- Accordion Toggle Handlers ---
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
