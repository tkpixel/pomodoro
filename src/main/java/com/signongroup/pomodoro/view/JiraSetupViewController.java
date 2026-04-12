package com.signongroup.pomodoro.view;

import com.signongroup.pomodoro.viewmodel.JiraSetupViewModel;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ResourceBundle;

@Prototype
public class JiraSetupViewController implements Initializable {

    @FXML
    private TextField urlField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField tokenFieldMasked;

    @FXML
    private TextField tokenFieldVisible;

    @FXML
    private FontIcon visibilityIcon;

    @FXML
    private Label statusLabel;

    @FXML
    private Button connectButton;

    private boolean isPasswordVisible = false;

    private final JiraSetupViewModel viewModel;
    private final WindowManager windowManager;

    @Inject
    public JiraSetupViewController(JiraSetupViewModel viewModel, WindowManager windowManager) {
        this.viewModel = viewModel;
        this.windowManager = windowManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        urlField.textProperty().bindBidirectional(viewModel.urlProperty());
        emailField.textProperty().bindBidirectional(viewModel.emailProperty());

        tokenFieldMasked.textProperty().bindBidirectional(viewModel.tokenProperty());
        tokenFieldVisible.textProperty().bindBidirectional(viewModel.tokenProperty());

        statusLabel.textProperty().bind(viewModel.statusMessageProperty());

        viewModel.isSuccessProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                statusLabel.setStyle("-fx-text-fill: -fx-primary; -fx-font-size: 13px; -fx-font-weight: bold;");
            } else {
                statusLabel.setStyle("-fx-text-fill: -fx-danger-emphasis; -fx-font-size: 13px; -fx-font-weight: bold;");
            }
        });

        connectButton.disableProperty().bind(Bindings.createBooleanBinding(
            () -> urlField.getText().isEmpty() || emailField.getText().isEmpty() || tokenFieldMasked.getText().isEmpty() || viewModel.isConnectingProperty().get(),
            urlField.textProperty(), emailField.textProperty(), tokenFieldMasked.textProperty(), viewModel.isConnectingProperty()
        ));
    }

    @FXML
    public void handleBack(ActionEvent event) {
        windowManager.showMainView();
    }

    @FXML
    public void handleConnect(ActionEvent event) {
        viewModel.connectAndTest();
    }

    @FXML
    public void toggleTokenVisibility(ActionEvent event) {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            tokenFieldMasked.setVisible(false);
            tokenFieldVisible.setVisible(true);
            visibilityIcon.setIconLiteral("fltral-eye-off-20");
        } else {
            tokenFieldMasked.setVisible(true);
            tokenFieldVisible.setVisible(false);
            visibilityIcon.setIconLiteral("fltral-eye-20");
        }
    }
}
