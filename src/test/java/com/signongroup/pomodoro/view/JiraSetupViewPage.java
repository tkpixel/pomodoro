package com.signongroup.pomodoro.view;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.kordamp.ikonli.javafx.FontIcon;
import org.testfx.api.FxRobot;

public class JiraSetupViewPage {

    private final FxRobot robot;

    public JiraSetupViewPage(FxRobot robot) {
        this.robot = robot;
    }

    public JiraSetupViewPage enterUrl(String url) {
        robot.clickOn("#urlField");
        robot.write(url);
        return this;
    }

    public JiraSetupViewPage enterEmail(String email) {
        robot.clickOn("#emailField");
        robot.write(email);
        return this;
    }

    public JiraSetupViewPage enterToken(String token) {
        robot.clickOn("#tokenFieldMasked");
        robot.write(token);
        return this;
    }

    public JiraSetupViewPage clickConnect() {
        robot.clickOn("#connectButton");
        return this;
    }

    public JiraSetupViewPage toggleTokenVisibility() {
        robot.clickOn("#visibilityIcon");
        return this;
    }

    public void clearFields() {
        robot.clickOn("#urlField").eraseText(100);
        robot.clickOn("#emailField").eraseText(100);
        robot.clickOn("#tokenFieldMasked").eraseText(100);
    }

    public String getUrlFieldText() {
        return robot.lookup("#urlField").queryAs(TextField.class).getText();
    }

    public String getEmailFieldText() {
        return robot.lookup("#emailField").queryAs(TextField.class).getText();
    }

    public String getTokenMaskedText() {
        return robot.lookup("#tokenFieldMasked").queryAs(PasswordField.class).getText();
    }

    public String getTokenVisibleText() {
        return robot.lookup("#tokenFieldVisible").queryAs(TextField.class).getText();
    }

    public String getStatusText() {
        return robot.lookup("#statusLabel").queryAs(Label.class).getText();
    }

    public boolean isConnectButtonDisabled() {
        return robot.lookup("#connectButton").queryAs(Button.class).isDisabled();
    }

    public FontIcon getVisibilityIcon() {
        return robot.lookup("#visibilityIcon").queryAs(FontIcon.class);
    }

    public void clickBack() {
        robot.clickOn(node -> node instanceof FontIcon icon && "fltral-chevron-left-20".equals(icon.getIconLiteral()));
    }
}
