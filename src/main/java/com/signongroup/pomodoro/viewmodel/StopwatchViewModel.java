package com.signongroup.pomodoro.viewmodel;

import com.signongroup.pomodoro.service.ActiveTaskService;
import com.signongroup.pomodoro.service.JiraBoardService;
import com.signongroup.pomodoro.service.TimerService;
import com.signongroup.pomodoro.service.TrackingService;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.ObjectProperty;

@Singleton
public class StopwatchViewModel {

    private final StringProperty timerText = new SimpleStringProperty("00:00");
    private final BooleanProperty isRunning = new SimpleBooleanProperty(false);
    private final TimerService timerService;
    private final JiraBoardService jiraBoardService;
    private final ActiveTaskService activeTaskService;
    private final MainViewModel mainViewModel;
    private final TrackingService trackingService;

    private int elapsedSeconds = 0;
    private int unloggedSeconds = 0;

    @jakarta.inject.Inject
    public StopwatchViewModel(JiraBoardService jiraBoardService, ActiveTaskService activeTaskService, MainViewModel mainViewModel, TrackingService trackingService) {
        this.timerService = new TimerService();
        this.jiraBoardService = jiraBoardService;
        this.activeTaskService = activeTaskService;
        this.mainViewModel = mainViewModel;
        this.trackingService = trackingService;

        this.timerService.setTickCallback(() -> Platform.runLater(this::tick));

        this.timerText.addListener((obs, oldVal, newVal) -> pushTrackingState());
        this.isRunning.addListener((obs, oldVal, newVal) -> pushTrackingState());
        this.trackingService.activeModeProperty().addListener((obs, oldVal, newVal) -> pushTrackingState());

        // Explicitly push initial state upon initialization
        pushTrackingState();
    }

    private void pushTrackingState() {
        if (trackingService.getActiveMode() == TrackingService.TrackingMode.STOPWATCH) {
            trackingService.activeTimeProperty().set(timerText.get());
            trackingService.isRunningProperty().set(isRunning.get());
            trackingService.setOnToggleTimer(this::toggleTimer);
            trackingService.setOnResetTimer(this::resetTimer);
        }
    }

    private void tick() {
        elapsedSeconds++;
        unloggedSeconds++;
        updateUI();
    }

    private void updateUI() {
        int hours = elapsedSeconds / 3600;
        int minutes = (elapsedSeconds % 3600) / 60;
        int seconds = elapsedSeconds % 60;

        if (hours > 0) {
            timerText.set(String.format("%02d:%02d:%02d", hours, minutes, seconds));
        } else {
            timerText.set(String.format("%02d:%02d", minutes, seconds));
        }
    }

    public void startTimer() {
        isRunning.set(true);
        timerService.start();
    }

    public void pauseTimer() {
        isRunning.set(false);
        timerService.pause();

        if (unloggedSeconds > 0 && activeTaskService.getActiveTask() != null) {
            jiraBoardService.addWorklog(activeTaskService.getActiveTask().taskKeyProperty().get(), unloggedSeconds);
            activeTaskService.getActiveTask().addTimeSpent(unloggedSeconds);
            unloggedSeconds = 0;
        }
    }

    public void toggleTimer() {
        if (isRunning.get()) {
            pauseTimer();
        } else {
            startTimer();
        }
    }

    public void resetTimer() {
        isRunning.set(false);
        timerService.pause();
        elapsedSeconds = 0;
        unloggedSeconds = 0;
        updateUI();
    }

    public StringProperty timerTextProperty() { return timerText; }
    public BooleanProperty isRunningProperty() { return isRunning; }

    public StringProperty clearedTodayTextProperty() {
        return mainViewModel.clearedTodayTextProperty();
    }

    public ObjectProperty<TaskCardViewModel> activeTaskProperty() {
        return activeTaskService.activeTaskProperty();
    }
}
