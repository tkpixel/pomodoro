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
public class SettingsViewTest {

    private ApplicationContext context;
    private SettingsViewPage page;

    @Start
    public void start(Stage stage) throws Exception {
        context = ApplicationContext.run();

        Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());

        // Load the new unified Settings view
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/signongroup/pomodoro/view/SettingsView.fxml"));
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
        page = new SettingsViewPage(robot);
    }

    @AfterEach
    public void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    public void testHappyPathChangeDurationSettings() {
        page.clickExpandDuration();
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);

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
        page.clickExpandDuration();
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);

        // Test boundary limits for max sessions by decreasing a lot
        for (int i = 0; i < 10; i++) {
            page.clickDecMaxSessions();
            WaitForAsyncUtils.sleep(50, TimeUnit.MILLISECONDS);
        }

        int minMaxSessions = Integer.parseInt(page.getMaxSessionsValue());
        assertThat(minMaxSessions).isGreaterThanOrEqualTo(1);

        // Test decreasing duration to minimum
        for (int i = 0; i < 20; i++) {
            page.clickDecFocus();
            WaitForAsyncUtils.sleep(50, TimeUnit.MILLISECONDS);
        }

        // Parsing out "05:00" string
        String focusStr = page.getFocusSessionValue();
        int minFocus = Integer.parseInt(focusStr.split(":")[0]);
        assertThat(minFocus).isGreaterThanOrEqualTo(1);
    }

    @Test
    public void testHappyPathJiraSetup() {
        page.clickExpandJira();
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);

        // Erase any saved fields to test empty-disabled state
        page.clearFields();

        // Initially, the connect button should be disabled because fields are empty
        assertThat(page.isConnectButtonDisabled()).isTrue();

        page.enterUrl("https://example.atlassian.net");
        page.enterEmail("test@example.com");
        page.enterToken("dummy-token");

        // After filling, it should be enabled
        assertThat(page.isConnectButtonDisabled()).isFalse();

        FxRobot robot = new FxRobot();
        robot.interact(() -> robot.lookup("#connectButton").query().getScene().getWindow().setHeight(1500));
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);
        page.clickConnect();

        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);
        assertThat(page.getStatusText()).isNotNull();
    }

    @Test
    public void testEdgeCaseTokenVisibility() {
        page.clickExpandJira();
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);

        page.clearFields();
        page.enterToken("secret123");

        // Initial visibility icon
        assertThat(page.getVisibilityIcon().getIconLiteral()).isEqualTo("fltfal-eye-show-20");

        // Toggle visibility
        page.toggleTokenVisibility();
        WaitForAsyncUtils.sleep(200, TimeUnit.MILLISECONDS);

        // Visibility icon should be updated
        assertThat(page.getVisibilityIcon().getIconLiteral()).isEqualTo("fltfal-eye-hide-20");

        // Verify the visible field matches masked text input logic and is rendered.
        assertThat(page.getTokenVisibleText()).isEqualTo("secret123");
    }
}
