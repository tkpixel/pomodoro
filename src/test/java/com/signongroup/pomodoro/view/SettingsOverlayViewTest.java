package com.signongroup.pomodoro.view;

import atlantafx.base.theme.NordDark;
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
public class SettingsOverlayViewTest {

    private ApplicationContext context;
    private SettingsOverlayViewPage page;

    @Start
    public void start(Stage stage) throws Exception {
        context = ApplicationContext.run();

        Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());

        // Use MainView because SettingsOverlay is an included component and testing
        // it standalone might break due to its visibility binding and context.
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
        MainViewPage mainPage = new MainViewPage(robot);
        mainPage.clickOpenSettings();
        // The animation takes 300ms. Wait slightly longer for the bottomSheet to fully settle at translateY=0
        WaitForAsyncUtils.sleep(800, TimeUnit.MILLISECONDS);
        page = new SettingsOverlayViewPage(robot);
    }

    @AfterEach
    public void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    public void testHappyPathChangeSettings() {
        // Initial state check
        String initFocus = page.getFocusSessionValue();
        assertThat(initFocus).isNotBlank();

        // Increment focus session
        page.clickIncFocus();
        WaitForAsyncUtils.sleep(200, TimeUnit.MILLISECONDS);
        String newFocus = page.getFocusSessionValue();

        // Assert it changed (usually +5, but we just check it changed)
        assertThat(newFocus).isNotEqualTo(initFocus);

        // Same for max sessions
        String initMax = page.getMaxSessionsValue();
        page.clickIncMaxSessions();
        WaitForAsyncUtils.sleep(200, TimeUnit.MILLISECONDS);
        assertThat(page.getMaxSessionsValue()).isNotEqualTo(initMax);
    }

    @Test
    public void testEdgeCaseBoundaryLimits() {
        // Assume default max sessions is 4 and limit is something > 1 but we can test decreasing it to 1
        for (int i = 0; i < 10; i++) {
            page.clickDecMaxSessions();
            WaitForAsyncUtils.sleep(50, TimeUnit.MILLISECONDS);
        }

        // Validate it didn't go below 1 or 0
        int minMaxSessions = Integer.parseInt(page.getMaxSessionsValue());
        assertThat(minMaxSessions).isGreaterThanOrEqualTo(1);

        // Test decreasing duration to minimum
        for (int i = 0; i < 20; i++) {
            page.clickDecFocus();
            WaitForAsyncUtils.sleep(50, TimeUnit.MILLISECONDS);
        }

        int minFocus = Integer.parseInt(page.getFocusSessionValue());
        assertThat(minFocus).isGreaterThanOrEqualTo(1);
    }
}
