package com.signongroup.pomodoro.view;

import atlantafx.base.theme.NordDark;
import com.signongroup.pomodoro.viewmodel.PomodoroViewModel;
import io.micronaut.context.ApplicationContext;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ApplicationExtension.class)
public class MainViewTest {

    private ApplicationContext context;
    private MainViewPage mainViewPage;

    @Start
    public void start(Stage stage) throws Exception {
        context = ApplicationContext.run();

        Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/signongroup/pomodoro/view/MainView.fxml"));
        loader.setControllerFactory(context::getBean);

        Parent root = loader.load();
        String customCss = Objects.requireNonNull(getClass().getResource("/css/corporate-design.css")).toExternalForm();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(customCss);

        WindowManager windowManager = context.getBean(WindowManager.class);
        windowManager.init(stage, scene);

        stage.setScene(scene);
        stage.show();
    }

    @BeforeEach
    public void setUp(FxRobot robot) {
        mainViewPage = new MainViewPage(robot);
    }

    @AfterEach
    public void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    public void testHappyPathPlayPause() {
        // Initial state
        assertThat(mainViewPage.getPlayIcon().getIconLiteral()).isEqualTo("fltfmz-play-20");

        // Start timer
        mainViewPage.clickPlayPause();

        // Wait for UI to update icon
        WaitForAsyncUtils.sleep(200, TimeUnit.MILLISECONDS);
        assertThat(mainViewPage.getPlayIcon().getIconLiteral()).isEqualTo("fltfmz-pause-20");

        // Pause timer
        mainViewPage.clickPlayPause();
        WaitForAsyncUtils.sleep(200, TimeUnit.MILLISECONDS);
        assertThat(mainViewPage.getPlayIcon().getIconLiteral()).isEqualTo("fltfmz-play-20");
    }

    @Test
    public void testEdgeCaseSkipPhase() {
        // Assume default timer starts with some duration, e.g., 25:00
        String initialTimerText = mainViewPage.getTimerText();
        assertThat(initialTimerText).isNotBlank();

        // Initial session text
        String initialSession = mainViewPage.getSessionText();
        assertThat(initialSession).isNotBlank();

        // Play should be pause-able or resume-able
        assertThat(mainViewPage.getPlayIcon().getIconLiteral()).isEqualTo("fltfmz-play-20");

        // Timer state begins as Focus Time, so skip button is invisible.
    }
}
