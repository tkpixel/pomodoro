package com.signongroup.template;

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

public class TemplateApplication extends Application {

    @Nullable
    private ApplicationContext context;

    @Override
    public void init() {
        context = ApplicationContext.run();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/signongroup/template/view/MainView.fxml"));

        if (context != null) {
            loader.setControllerFactory(context::getBean);
        }

        Parent root = loader.load();

        // Überschreiben mit Corporate Design (CD) Farben:
        String customCss = Objects.requireNonNull(getClass().getResource("/css/corporate-design.css")).toExternalForm();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(customCss);

        primaryStage.setTitle("Template-Tool");
        // TODO
        //  primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Logo.png"))));

        // Get screen dimensions
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double screenWidth = screenBounds.getWidth();
        double screenHeight = screenBounds.getHeight();

        // Set window size relative to screen size (70% width, 75% height)
        double windowWidth = screenWidth * 0.7;
        double windowHeight = screenHeight * 0.75;

        primaryStage.setWidth(windowWidth);
        primaryStage.setHeight(windowHeight);
        primaryStage.setMinWidth(1500.0);
        primaryStage.setMinHeight(600.0);

        // Center the window on screen
        primaryStage.setX((screenWidth - windowWidth) / 2);
        primaryStage.setY((screenHeight - windowHeight) / 2);

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

