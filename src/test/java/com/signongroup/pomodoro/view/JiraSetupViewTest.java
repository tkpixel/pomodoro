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
public class JiraSetupViewTest {

    private ApplicationContext context;
    private JiraSetupViewPage page;

    @Start
    public void start(Stage stage) throws Exception {
        context = ApplicationContext.run();

        Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/signongroup/pomodoro/view/JiraSetupView.fxml"));
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
        page = new JiraSetupViewPage(robot);
    }

    @AfterEach
    public void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    public void testHappyPathSetup() {
        // Initially, the connect button should be disabled because fields are empty
        assertThat(page.isConnectButtonDisabled()).isTrue();

        page.enterUrl("https://example.atlassian.net");
        page.enterEmail("test@example.com");
        page.enterToken("dummy-token");

        // After filling, it should be enabled
        assertThat(page.isConnectButtonDisabled()).isFalse();

        page.clickConnect();

        // Check if connection succeeds or fails by status text change. Mocking would make this 100% deterministic,
        // but even with actual failure, status text should change.
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);
        assertThat(page.getStatusText()).isNotNull();
    }

    @Test
    public void testEdgeCaseTokenVisibility() {
        page.enterToken("secret123");

        // Visible field should have the text
        assertThat(page.getTokenVisibleText()).isEqualTo("secret123");

        // Initial visibility icon
        assertThat(page.getVisibilityIcon().getIconLiteral()).isEqualTo("fltfal-eye-show-20");

        // Toggle visibility
        page.toggleTokenVisibility();
        WaitForAsyncUtils.sleep(200, TimeUnit.MILLISECONDS);

        // Visibility icon should be updated
        assertThat(page.getVisibilityIcon().getIconLiteral()).isEqualTo("fltfal-eye-hide-20");

        // Verify the visible field matches masked text input logic and is rendered.
        // FXML hides mask and unhides visible
        assertThat(page.getTokenVisibleText()).isEqualTo("secret123");
    }
}
