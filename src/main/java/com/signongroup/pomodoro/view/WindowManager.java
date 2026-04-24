package com.signongroup.pomodoro.view;

import com.signongroup.pomodoro.service.TrackingService;
import io.micronaut.context.ApplicationContext;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class WindowManager {

    private static final Logger log = LoggerFactory.getLogger(WindowManager.class);

    private Stage primaryStage;
    private final ApplicationContext context;
    private Scene scene;
    private final TrackingService trackingService;

    @Inject
    public WindowManager(ApplicationContext context, TrackingService trackingService) {
        this.context = context;
        this.trackingService = trackingService;
    }

    public void init(Stage primaryStage, Scene scene) {
        this.primaryStage = primaryStage;
        this.scene = scene;
    }

    public void showPomodoroView() {
        trackingService.setActiveMode(TrackingService.TrackingMode.POMODORO);
    }

    public void showSettingsView() {
        switchView("/com/signongroup/pomodoro/view/SettingsView.fxml");
    }

    public void showStopwatchView() {
        trackingService.setActiveMode(TrackingService.TrackingMode.STOPWATCH);
    }

    public void showActiveTimerView() {
        // First ensure the mode is correctly set without triggering a scene switch.
        // Then load the MainView shell which automatically loads the right sub-view.
        switchView("/com/signongroup/pomodoro/view/MainView.fxml");
    }

    public void showJiraBoardView() {
        switchView("/com/signongroup/pomodoro/view/jira/JiraBoardView.fxml");
    }

    public void showMiniTimerView() {
        switchView("/com/signongroup/pomodoro/view/MiniTimerView.fxml");
    }

    public void toggleMiniMode(boolean isMini) {
        if (primaryStage == null) return;
        if (isMini) {
            primaryStage.setMinWidth(288);
            primaryStage.setMinHeight(125);
            primaryStage.setMaxWidth(288);
            primaryStage.setMaxHeight(125);
            primaryStage.setWidth(288);
            primaryStage.setHeight(125);
            showMiniTimerView();
            primaryStage.setAlwaysOnTop(true);
        } else {
            primaryStage.setMinWidth(684);
            primaryStage.setMinHeight(552);
            primaryStage.setMaxWidth(Double.MAX_VALUE);
            primaryStage.setMaxHeight(Double.MAX_VALUE);
            primaryStage.setWidth(684);
            primaryStage.setHeight(552);
            showActiveTimerView();
            primaryStage.setAlwaysOnTop(false);
        }
    }

    private void switchView(String fxmlPath) {
        if (scene == null) {
            log.error("switchView called but scene is null – was WindowManager.init() called?");
            return;
        }
        try {
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                throw new IllegalStateException("Cannot find FXML file: " + fxmlPath);
            }
            FXMLLoader loader = new FXMLLoader(resource);
            // Bulletproof instantiation: Explicitly set the ClassLoader to ensure ServiceLoader
            // (which Ikonli uses internally) can find the registered IconPack service providers
            // across the module boundary within this application context.
            loader.setClassLoader(getClass().getClassLoader());
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();
            scene.setRoot(root);
        } catch (Exception e) {
            log.error("Failed to load view: {}", fxmlPath, e);
            throw new RuntimeException("Failed to load view: " + fxmlPath, e);
        }
    }
}
