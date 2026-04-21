package com.signongroup.pomodoro.viewmodel;

import com.signongroup.pomodoro.service.ActiveTaskService;
import com.signongroup.pomodoro.service.JiraBoardService;
import com.signongroup.pomodoro.service.TimerService;
import com.signongroup.pomodoro.service.TrackingService;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * ViewModel für die Hauptansicht (MVVM-Pattern).
 * Wird von Micronaut als Singleton verwaltet.
 */
@Singleton
public class MainViewModel {

    public enum TimerState {
        READY,
        FOCUS_RUNNING,
        BREAK_SHORT,
        BREAK_LONG
    }

    private final StringProperty timerText = new SimpleStringProperty("25:00");
    private final StringProperty sessionText = new SimpleStringProperty("1 / 4");
    private final StringProperty clearedTodayText = new SimpleStringProperty("00");
    private final StringProperty nextBreakText = new SimpleStringProperty("05:00");
    private final DoubleProperty timerProgress = new SimpleDoubleProperty(1.0);
    private final DoubleProperty breakProgress = new SimpleDoubleProperty(0.0);
    private final BooleanProperty isRunning = new SimpleBooleanProperty(false);
    private final ObjectProperty<TimerState> timerState = new SimpleObjectProperty<>(TimerState.READY);

    private int focusTimeSeconds;
    private int shortBreakSeconds;
    private int longBreakSeconds;
    private int maxSessions;

    private int currentSession = 1;
    private int clearedToday = 0;
    private int timeRemainingSeconds;

    private final TimerService timerService;
    private final SettingsViewModel settingsViewModel;
    private final JiraBoardService jiraBoardService;
    private final ActiveTaskService activeTaskService;
    private final TrackingService trackingService;

    @jakarta.inject.Inject
    public MainViewModel(SettingsViewModel settingsViewModel, TimerService timerService, JiraBoardService jiraBoardService, ActiveTaskService activeTaskService, TrackingService trackingService) {
        this.settingsViewModel = settingsViewModel;
        this.timerService = timerService;
        this.jiraBoardService = jiraBoardService;
        this.activeTaskService = activeTaskService;
        this.trackingService = trackingService;

        this.timerService.setTickCallback(() -> Platform.runLater(this::tick));

        // Initialize from settings
        updateSettingsFromViewModel();

        // Listen for changes from SettingsViewModel
        this.settingsViewModel.focusSessionMinutesProperty().addListener((obs, oldVal, newVal) -> {
            updateSettingsFromViewModel();
            if (timerState.get() == TimerState.READY) {
                timeRemainingSeconds = focusTimeSeconds;
                updateUI();
            }
        });
        this.settingsViewModel.shortBreakMinutesProperty().addListener((obs, oldVal, newVal) -> updateSettingsFromViewModel());
        this.settingsViewModel.longBreakMinutesProperty().addListener((obs, oldVal, newVal) -> updateSettingsFromViewModel());
        this.settingsViewModel.maxSessionCountProperty().addListener((obs, oldVal, newVal) -> {
            updateSettingsFromViewModel();
            updateUI();
        });

        this.timerText.addListener((obs, oldVal, newVal) -> pushTrackingState());
        this.isRunning.addListener((obs, oldVal, newVal) -> pushTrackingState());
        this.trackingService.activeModeProperty().addListener((obs, oldVal, newVal) -> pushTrackingState());

        timeRemainingSeconds = focusTimeSeconds;
        updateUI();
    }

    private void pushTrackingState() {
        if (trackingService.getActiveMode() == TrackingService.TrackingMode.POMODORO) {
            trackingService.activeTimeProperty().set(timerText.get());
            trackingService.isRunningProperty().set(isRunning.get());
            trackingService.setOnToggleTimer(this::toggleTimer);
            trackingService.setOnResetTimer(this::resetCurrentPhase);
        }
    }

    private void updateSettingsFromViewModel() {
        this.focusTimeSeconds = settingsViewModel.focusSessionMinutesProperty().get() * 60;
        this.shortBreakSeconds = settingsViewModel.shortBreakMinutesProperty().get() * 60;
        this.longBreakSeconds = settingsViewModel.longBreakMinutesProperty().get() * 60;
        this.maxSessions = settingsViewModel.maxSessionCountProperty().get();
    }

    private void tick() {
        if (timeRemainingSeconds > 0) {
            timeRemainingSeconds--;
            updateUI();
        } else {
            handleTimerComplete();
        }
    }

    private void handleTimerComplete() {
        pauseTimer();
        if (timerState.get() == TimerState.FOCUS_RUNNING) {
            if (activeTaskService.getActiveTask() != null) {
                jiraBoardService.addWorklog(activeTaskService.getActiveTask().taskKeyProperty().get(), focusTimeSeconds);
                activeTaskService.getActiveTask().addTimeSpent(focusTimeSeconds);
            }
            clearedToday++;
            if (currentSession >= maxSessions) {
                currentSession = 1;
                timeRemainingSeconds = longBreakSeconds;
                timerState.set(TimerState.BREAK_LONG);
            } else {
                currentSession++;
                timeRemainingSeconds = shortBreakSeconds;
                timerState.set(TimerState.BREAK_SHORT);
            }
            updateUI();
            startTimer();
        } else if (timerState.get() == TimerState.BREAK_SHORT || timerState.get() == TimerState.BREAK_LONG) {
            if (timerState.get() == TimerState.BREAK_SHORT && activeTaskService.getActiveTask() != null) {
                jiraBoardService.addWorklog(activeTaskService.getActiveTask().taskKeyProperty().get(), shortBreakSeconds);
                activeTaskService.getActiveTask().addTimeSpent(shortBreakSeconds);
            }
            resetToReady();
        }
    }

    private void updateUI() {
        if (timerState.get() == TimerState.BREAK_SHORT || timerState.get() == TimerState.BREAK_LONG) {
            int maxTime = (timerState.get() == TimerState.BREAK_LONG) ? longBreakSeconds : shortBreakSeconds;
            nextBreakText.set(formatTime(timeRemainingSeconds));
            breakProgress.set((double) timeRemainingSeconds / maxTime);
            timerText.set("00:00");
            timerProgress.set(0.0);
        } else {
            timerText.set(formatTime(timeRemainingSeconds));
            timerProgress.set((double) timeRemainingSeconds / focusTimeSeconds);
            int maxTime = (currentSession == maxSessions) ? longBreakSeconds : shortBreakSeconds;
            nextBreakText.set(formatTime(maxTime));
            breakProgress.set(0.0);
        }
        sessionText.set(currentSession + " / " + maxSessions);
        clearedTodayText.set(String.format("%02d", clearedToday));
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public void startTimer() {
        if (timerState.get() == TimerState.READY) {
            timerState.set(TimerState.FOCUS_RUNNING);
        }
        isRunning.set(true);
        timerService.start();
    }

    public void pauseTimer() {
        isRunning.set(false);
        timerService.pause();
    }

    public void toggleTimer() {
        if (isRunning.get()) {
            pauseTimer();
        } else {
            startTimer();
        }
    }

    public void skipBreak() {
        if (timerState.get() == TimerState.BREAK_SHORT || timerState.get() == TimerState.BREAK_LONG) {
            pauseTimer();
            resetToReady();
        }
    }

    public void resetCurrentPhase() {
        TimerState currentState = timerState.get();

        if (currentState == TimerState.BREAK_SHORT || currentState == TimerState.BREAK_LONG) {
            int maxTime = (currentSession == 1 && clearedToday > 0) ? longBreakSeconds : shortBreakSeconds;
            timeRemainingSeconds = maxTime;
            breakProgress.set(1.0);
        } else {
            timeRemainingSeconds = focusTimeSeconds;
            timerState.set(TimerState.READY);
            timerProgress.set(1.0);
        }

        pauseTimer();
        updateUI();
    }

    private void resetToReady() {
        timerState.set(TimerState.READY);
        timeRemainingSeconds = focusTimeSeconds;
        updateUI();
    }

    // --- Active Task Routing ---

    public void setActiveTask(TaskCardViewModel task) {
        this.activeTaskService.setActiveTask(task);
    }

    public ObjectProperty<TaskCardViewModel> activeTaskProperty() {
        return this.activeTaskService.activeTaskProperty();
    }

    public TaskCardViewModel getActiveTask() {
        return this.activeTaskService.getActiveTask();
    }

    // --- Getters & Setters / Properties ---

    public StringProperty timerTextProperty() { return timerText; }
    public String getTimerText() { return timerText.get(); }
    public void setTimerText(String value) { this.timerText.set(value); }

    public StringProperty sessionTextProperty() { return sessionText; }
    public String getSessionText() { return sessionText.get(); }
    public void setSessionText(String value) { this.sessionText.set(value); }

    public StringProperty clearedTodayTextProperty() { return clearedTodayText; }
    public String getClearedTodayText() { return clearedTodayText.get(); }
    public void setClearedTodayText(String value) { this.clearedTodayText.set(value); }

    public StringProperty nextBreakTextProperty() { return nextBreakText; }
    public String getNextBreakText() { return nextBreakText.get(); }
    public void setNextBreakText(String value) { this.nextBreakText.set(value); }

    public DoubleProperty timerProgressProperty() { return timerProgress; }
    public double getTimerProgress() { return timerProgress.get(); }
    public void setTimerProgress(double value) { this.timerProgress.set(value); }

    public DoubleProperty breakProgressProperty() { return breakProgress; }
    public double getBreakProgress() { return breakProgress.get(); }
    public void setBreakProgress(double value) { this.breakProgress.set(value); }

    public BooleanProperty isRunningProperty() { return isRunning; }
    public boolean getIsRunning() { return isRunning.get(); }

    public ObjectProperty<TimerState> timerStateProperty() { return timerState; }
    public TimerState getTimerState() { return timerState.get(); }

}
