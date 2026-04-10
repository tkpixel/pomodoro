package com.signongroup.pomodoro.view;

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

@Singleton
public class WindowManager {

    private Stage primaryStage;
    private final ApplicationContext context;
    private Scene scene;

    @Inject
    public WindowManager(ApplicationContext context) {
        this.context = context;
    }

    public void init(Stage primaryStage, Scene scene) {
        this.primaryStage = primaryStage;
        this.scene = scene;
    }

    public void showMainView() {
        switchView("/com/signongroup/pomodoro/view/MainView.fxml");
    }

    public void showJiraSetupView() {
        switchView("/com/signongroup/pomodoro/view/JiraSetupView.fxml");
    }

    private void switchView(String fxmlPath) {
        if (scene == null) return;
        try {
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                throw new IllegalStateException("Cannot find FXML file: " + fxmlPath);
            }
            FXMLLoader loader = new FXMLLoader(resource);
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load view: " + fxmlPath, e);
        }
    }
}
