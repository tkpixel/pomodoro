package com.signongroup.pomodoro;

import atlantafx.base.theme.NordDark;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.Nullable;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.Objects;

public class PomodoroApplication extends Application {

    @Nullable
    private ApplicationContext context;

    @Override
    public void init() {
        context = ApplicationContext.run();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/signongroup/pomodoro/view/MainView.fxml"));

        if (context != null) {
            loader.setControllerFactory(context::getBean);
        }

        Parent root = loader.load();

        // Überschreiben mit Corporate Design (CD) Farben:
        String customCss = Objects.requireNonNull(getClass().getResource("/css/corporate-design.css")).toExternalForm();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(customCss);

        primaryStage.setTitle("Pomodoro Timer");
        // TODO
        //  primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Logo.png"))));

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        if (context != null) {
            context.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

