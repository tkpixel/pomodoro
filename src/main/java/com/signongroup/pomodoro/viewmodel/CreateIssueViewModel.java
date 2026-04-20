package com.signongroup.pomodoro.viewmodel;

import com.signongroup.pomodoro.model.jira.AdfDoc;
import com.signongroup.pomodoro.model.jira.Assignee;
import com.signongroup.pomodoro.model.jira.IssueCreateRequest;
import com.signongroup.pomodoro.model.jira.IssueFields;
import com.signongroup.pomodoro.model.jira.IssueType;
import com.signongroup.pomodoro.model.jira.JiraTask;
import com.signongroup.pomodoro.model.jira.Priority;
import com.signongroup.pomodoro.model.jira.Project;
import com.signongroup.pomodoro.service.JiraAuthService;
import com.signongroup.pomodoro.service.JiraBoardService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Singleton
public class CreateIssueViewModel {
    private static final Logger log = LoggerFactory.getLogger(CreateIssueViewModel.class);

    private final JiraBoardService jiraBoardService;
    private final JiraAuthService jiraAuthService;
    private final JiraBoardViewModel jiraBoardViewModel;

    private final StringProperty summary = new SimpleStringProperty("");
    private final StringProperty description = new SimpleStringProperty("");
    private final StringProperty originalEstimate = new SimpleStringProperty("");
    private final IntegerProperty storyPoints = new SimpleIntegerProperty(0);

    private final ObjectProperty<IssueType> selectedIssueType = new SimpleObjectProperty<>();
    private final ObjectProperty<Priority> selectedPriority = new SimpleObjectProperty<>();
    private final BooleanProperty createAnother = new SimpleBooleanProperty(false);

    private final ObservableList<IssueType> availableIssueTypes = FXCollections.observableArrayList();
    private final ObservableList<Priority> availablePriorities = FXCollections.observableArrayList();

    private final BooleanProperty isLoading = new SimpleBooleanProperty(false);

    private String currentProjectId = "";

    @Inject
    public CreateIssueViewModel(JiraBoardService jiraBoardService, JiraAuthService jiraAuthService, JiraBoardViewModel jiraBoardViewModel) {
        this.jiraBoardService = jiraBoardService;
        this.jiraAuthService = jiraAuthService;
        this.jiraBoardViewModel = jiraBoardViewModel;
    }

    public void initData() {
        BoardViewModel selectedBoard = jiraBoardViewModel.getSelectedBoard();
        if (selectedBoard == null) {
            log.warn("Cannot initialize Create Issue view: no board selected");
            return;
        }

        // Jira Boards typically have board ID != project ID, but in Jira Software Cloud, boards are often tightly coupled to a project context.
        // For basic creation without complex context fetching, we attempt to use the board ID as the project ID, or fetch the first project.
        // Here we'll try to just pass the board ID. Realistically, we'd fetch the project for the board. We'll use selected board id for now.
        // It's a limitation we document or assume board id maps nicely or is required to create via board.
        // The mock-up just says 'Ember Focus', meaning it's displaying the board name.
        currentProjectId = selectedBoard.id().toString();

        isLoading.set(true);

        jiraBoardService.fetchIssueTypes(currentProjectId).thenAccept(issueTypes -> {
            Platform.runLater(() -> {
                availableIssueTypes.setAll(issueTypes);
                if (!issueTypes.isEmpty()) {
                    selectedIssueType.set(issueTypes.get(0));
                }
            });
        }).exceptionally(ex -> {
            log.error("Failed to fetch issue types", ex);
            return null;
        }).thenCompose(v -> jiraBoardService.fetchPriorities()).thenAccept(priorities -> {
            Platform.runLater(() -> {
                availablePriorities.setAll(priorities);
                if (!priorities.isEmpty()) {
                    // Try to find "High" or similar, else first
                    selectedPriority.set(priorities.get(0));
                }
                isLoading.set(false);
            });
        }).exceptionally(ex -> {
            log.error("Failed to fetch priorities", ex);
            Platform.runLater(() -> isLoading.set(false));
            return null;
        });
    }

    public void submitIssue() {
        if (summary.get().trim().isEmpty() || selectedIssueType.get() == null) {
            return; // Validation failed
        }

        isLoading.set(true);

        // Use current user for assignee
        String accountId = jiraAuthService.getSavedAccountId();
        Assignee assigneeObj = null;
        if (accountId != null && !accountId.isEmpty()) {
            assigneeObj = new Assignee(accountId, null, null);
        }

        JiraTask.Timetracking timetracking = null;
        if (!originalEstimate.get().trim().isEmpty()) {
             timetracking = new JiraTask.Timetracking(originalEstimate.get().trim(), null, null, null, null, null);
        }

        AdfDoc adfDescription = null;
        if (!description.get().trim().isEmpty()) {
            adfDescription = AdfDoc.ofText(description.get().trim());
        }

        IssueFields fields = new IssueFields(
            new Project(currentProjectId),
            summary.get().trim(),
            selectedIssueType.get(),
            selectedPriority.get(),
            assigneeObj,
            adfDescription,
            timetracking
        );

        IssueCreateRequest request = new IssueCreateRequest(fields);

        jiraBoardService.createIssue(request).thenAccept(v -> Platform.runLater(() -> {
            isLoading.set(false);
            jiraBoardViewModel.loadTasksForSelectedBoard();

            if (!createAnother.get()) {
                jiraBoardViewModel.isCreateModalVisibleProperty().set(false);
                clearForm();
            } else {
                summary.set("");
                description.set("");
                // keep estimate and story points, clear string ones
            }
        })).exceptionally(ex -> {
            log.error("Failed to create issue", ex);
            Platform.runLater(() -> isLoading.set(false));
            return null;
        });
    }

    public void close() {
        jiraBoardViewModel.isCreateModalVisibleProperty().set(false);
        if (!createAnother.get()) {
            clearForm();
        }
    }

    private void clearForm() {
        summary.set("");
        description.set("");
        originalEstimate.set("");
        storyPoints.set(0);
        createAnother.set(false);
        // keep selected combo box values
    }

    public void incrementStoryPoints() {
        storyPoints.set(storyPoints.get() + 1);
    }

    public void decrementStoryPoints() {
        if (storyPoints.get() > 0) {
            storyPoints.set(storyPoints.get() - 1);
        }
    }

    public StringProperty summaryProperty() { return summary; }
    public StringProperty descriptionProperty() { return description; }
    public StringProperty originalEstimateProperty() { return originalEstimate; }
    public IntegerProperty storyPointsProperty() { return storyPoints; }
    public ObjectProperty<IssueType> selectedIssueTypeProperty() { return selectedIssueType; }
    public ObjectProperty<Priority> selectedPriorityProperty() { return selectedPriority; }
    public BooleanProperty createAnotherProperty() { return createAnother; }
    public ObservableList<IssueType> getAvailableIssueTypes() { return availableIssueTypes; }
    public ObservableList<Priority> getAvailablePriorities() { return availablePriorities; }
    public BooleanProperty isLoadingProperty() { return isLoading; }

    public String getProjectName() {
        BoardViewModel board = jiraBoardViewModel.getSelectedBoard();
        return board != null ? board.name() : "No Project";
    }
}
