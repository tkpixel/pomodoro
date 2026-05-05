package com.signongroup.focus;

import atlantafx.base.theme.NordDark;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.Nullable;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.kordamp.ikonli.fluentui.FluentUiFilledAL;
import org.kordamp.ikonli.fluentui.FluentUiFilledMZ;
import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.fluentui.FluentUiRegularMZ;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Objects;

public class FocusApplication extends Application {

    @Nullable
    private ApplicationContext context;

    @Override
    public void init() {
        context = ApplicationContext.run();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());

        // Alle vier FluentUI-Handler im JavaFX-Thread explizit vorladen.
        // Ohne diese Zeilen werden fltfmz-* und fltrmz-* Icons als Rechtecke dargestellt.
        FontIcon.of(FluentUiFilledAL.ARCHIVE_20);
        FontIcon.of(FluentUiFilledMZ.MAXIMIZE_20);
        FontIcon.of(FluentUiRegularAL.ARCHIVE_20);
        FontIcon.of(FluentUiRegularMZ.MAXIMIZE_20);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/signongroup/focus/view/MainView.fxml"));
        loader.setClassLoader(getClass().getClassLoader());

        if (context != null) {
            loader.setControllerFactory(context::getBean);
        }

        Parent root = loader.load();

        // Überschreiben mit Corporate Design (CD) Farben:
        String customCss = Objects.requireNonNull(getClass().getResource("/css/corporate-design.css")).toExternalForm();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(customCss);

        if (context != null) {
            com.signongroup.focus.view.WindowManager windowManager = context.getBean(com.signongroup.focus.view.WindowManager.class);
            windowManager.init(primaryStage, scene);
        }

        primaryStage.setTitle("Focus Timer");
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Logo.png"))));

        primaryStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
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

