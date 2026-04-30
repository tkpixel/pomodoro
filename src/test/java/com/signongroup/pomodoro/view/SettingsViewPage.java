package com.signongroup.pomodoro.view;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.kordamp.ikonli.javafx.FontIcon;
import org.testfx.api.FxRobot;

public class SettingsViewPage {

  private final FxRobot robot;

  public SettingsViewPage(FxRobot robot) {
    this.robot = robot;
  }

  public String getFocusSessionValue() {
    return robot.lookup("#focusSessionLabel").queryAs(Label.class).getText();
  }

  public SettingsViewPage clickIncFocus() {
    robot
        .lookup("#focusSessionLabel")
        .queryAs(Label.class)
        .getParent()
        .getChildrenUnmodifiable()
        .stream()
        .filter(
            node ->
                node instanceof javafx.scene.control.Button btn
                    && btn.getGraphic() instanceof FontIcon icon
                    && "fltfal-add-20".equals(icon.getIconLiteral()))
        .findFirst()
        .ifPresent(robot::clickOn);
    return this;
  }

  public SettingsViewPage clickDecFocus() {
    robot
        .lookup("#focusSessionLabel")
        .queryAs(Label.class)
        .getParent()
        .getChildrenUnmodifiable()
        .stream()
        .filter(
            node ->
                node instanceof javafx.scene.control.Button btn
                    && btn.getGraphic() instanceof FontIcon icon
                    && "fltfal-line-horizontal-1-20".equals(icon.getIconLiteral()))
        .findFirst()
        .ifPresent(robot::clickOn);
    return this;
  }

  public String getMaxSessionsValue() {
    return robot.lookup("#maxSessionsLabel").queryAs(Label.class).getText();
  }

  public SettingsViewPage clickIncMaxSessions() {
    robot
        .lookup("#maxSessionsLabel")
        .queryAs(Label.class)
        .getParent()
        .getChildrenUnmodifiable()
        .stream()
        .filter(
            node ->
                node instanceof javafx.scene.control.Button btn
                    && btn.getGraphic() instanceof FontIcon icon
                    && "fltfal-add-20".equals(icon.getIconLiteral()))
        .findFirst()
        .ifPresent(robot::clickOn);
    return this;
  }

  public SettingsViewPage clickDecMaxSessions() {
    robot
        .lookup("#maxSessionsLabel")
        .queryAs(Label.class)
        .getParent()
        .getChildrenUnmodifiable()
        .stream()
        .filter(
            node ->
                node instanceof javafx.scene.control.Button btn
                    && btn.getGraphic() instanceof FontIcon icon
                    && "fltfal-line-horizontal-1-20".equals(icon.getIconLiteral()))
        .findFirst()
        .ifPresent(robot::clickOn);
    return this;
  }

  public SettingsViewPage clickExpandDuration() {
    robot.clickOn(
        node -> node instanceof FontIcon icon && "fltfmz-timer-20".equals(icon.getIconLiteral()));
    return this;
  }

  public SettingsViewPage clickExpandJira() {
    robot.clickOn(
        node ->
            node instanceof FontIcon icon && "fltfal-arrow-sync-20".equals(icon.getIconLiteral()));
    return this;
  }

  public SettingsViewPage enterUrl(String url) {
    robot.clickOn("#urlField");
    robot.write(url);
    return this;
  }

  public SettingsViewPage enterEmail(String email) {
    robot.clickOn("#emailField");
    robot.write(email);
    return this;
  }

  public SettingsViewPage enterToken(String token) {
    robot.clickOn("#tokenFieldMasked");
    robot.write(token);
    return this;
  }

  public SettingsViewPage clickConnect() {
    robot.clickOn("#connectButton");
    return this;
  }

  public SettingsViewPage toggleTokenVisibility() {
    robot.clickOn("#visibilityIcon");
    return this;
  }

  public void clearFields() {
    robot.interact(
        () -> {
          robot.lookup("#urlField").queryAs(TextField.class).setText("");
          robot.lookup("#emailField").queryAs(TextField.class).setText("");
          robot.lookup("#tokenFieldMasked").queryAs(PasswordField.class).setText("");
        });
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
}
