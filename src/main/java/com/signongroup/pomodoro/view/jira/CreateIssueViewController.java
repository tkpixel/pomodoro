package com.signongroup.pomodoro.view.jira;

import com.signongroup.pomodoro.model.jira.IssueType;
import com.signongroup.pomodoro.model.jira.Priority;
import com.signongroup.pomodoro.viewmodel.CreateIssueViewModel;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ResourceBundle;

@Prototype
public class CreateIssueViewController implements Initializable {

    @FXML private StackPane overlayRoot;
    @FXML private Label projectLabel;
    @FXML private TextField summaryField;
    @FXML private ComboBox<IssueType> issueTypeComboBox;
    @FXML private ComboBox<Priority> priorityComboBox;
    @FXML private TextArea descriptionArea;
    @FXML private TextField storyPointsField;
    @FXML private TextField originalEstimateField;
    @FXML private CheckBox createAnotherCheckBox;
    @FXML private Button createButton;

    private final CreateIssueViewModel viewModel;

    @Inject
    public CreateIssueViewController(CreateIssueViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Project Label
        projectLabel.setText(viewModel.getProjectName());

        // Bindings
        summaryField.textProperty().bindBidirectional(viewModel.summaryProperty());
        descriptionArea.textProperty().bindBidirectional(viewModel.descriptionProperty());
        originalEstimateField.textProperty().bindBidirectional(viewModel.originalEstimateProperty());
        createAnotherCheckBox.selectedProperty().bindBidirectional(viewModel.createAnotherProperty());

        // Story Points
        storyPointsField.textProperty().bindBidirectional(viewModel.storyPointsProperty(), new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                return object == null ? "0" : object.toString();
            }

            @Override
            public Number fromString(String string) {
                try {
                    return Integer.parseInt(string);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        });

        // ComboBoxes
        issueTypeComboBox.setItems(viewModel.getAvailableIssueTypes());
        issueTypeComboBox.valueProperty().bindBidirectional(viewModel.selectedIssueTypeProperty());
        issueTypeComboBox.setCellFactory(listView -> new IssueTypeCell());
        issueTypeComboBox.setButtonCell(new IssueTypeCell());

        priorityComboBox.setItems(viewModel.getAvailablePriorities());
        priorityComboBox.valueProperty().bindBidirectional(viewModel.selectedPriorityProperty());
        priorityComboBox.setCellFactory(listView -> new PriorityCell());
        priorityComboBox.setButtonCell(new PriorityCell());

        // Visibility
        // The modal visibility binding will be handled by JiraBoardViewController since it includes this component
        // But we load data when it becomes visible
        overlayRoot.visibleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                projectLabel.setText(viewModel.getProjectName());
                viewModel.initData();
            }
        });

        // UI updates based on loading state
        viewModel.isLoadingProperty().addListener((obs, oldVal, newVal) -> {
            createButton.setDisable(newVal);
            if (newVal) {
                createButton.setText("Creating...");
            } else {
                createButton.setText("Create Issue");
            }
        });
    }

    @FXML
    private void handleCancel() {
        viewModel.close();
    }

    @FXML
    private void handleCreate() {
        viewModel.submitIssue();
    }

    @FXML
    private void handleDecrementStoryPoints() {
        viewModel.decrementStoryPoints();
    }

    @FXML
    private void handleIncrementStoryPoints() {
        viewModel.incrementStoryPoints();
    }

    // Custom Cell Factories to show icons + text if desired, or just text matching mockup
    private static class IssueTypeCell extends ListCell<IssueType> {
        @Override
        protected void updateItem(IssueType item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item.name().toUpperCase());
                FontIcon icon = new FontIcon("fltfal-document-24");
                icon.setIconSize(16);
                icon.setStyle("-fx-icon-color: -fx-tertiary;");
                setGraphic(icon);
            }
        }
    }

    private static class PriorityCell extends ListCell<Priority> {
        @Override
        protected void updateItem(Priority item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item.name().toUpperCase());
                FontIcon icon = new FontIcon("fltfal-arrow-up-24"); // Approximate keyboard_double_arrow_up
                icon.setIconSize(16);
                icon.setStyle("-fx-icon-color: #ff716c;");
                setGraphic(icon);
            }
        }
    }
}
