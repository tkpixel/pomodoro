package com.signongroup.template.view;

import com.signongroup.template.viewmodel.MainViewModel;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller für die Hauptansicht (MVVM-Pattern).
 * Micronaut injiziert das ViewModel via @Inject.
 */
@Singleton
public class MainViewController implements Initializable {

    @FXML
    private Label greetingLabel;

    @FXML
    private Button actionButton;

    private final MainViewModel viewModel;

    @Inject
    public MainViewController(MainViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // View an ViewModel binden
        greetingLabel.textProperty().bind(viewModel.greetingProperty());
    }

    @FXML
    private void onActionButtonClicked() {
        viewModel.updateGreeting();
    }
}
