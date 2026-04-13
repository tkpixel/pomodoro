package com.signongroup.pomodoro.view;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.testfx.api.FxRobot;

public class SettingsOverlayViewPage {

    private final FxRobot robot;

    public SettingsOverlayViewPage(FxRobot robot) {
        this.robot = robot;
    }

    public StackPane getOverlayRoot() {
        return robot.lookup("#overlayRoot").queryAs(StackPane.class);
    }

    public VBox getBottomSheet() {
        return robot.lookup("#bottomSheet").queryAs(VBox.class);
    }

    public String getFocusSessionValue() {
        return robot.lookup("#focusSessionLabel").queryAs(Label.class).getText();
    }

    public SettingsOverlayViewPage clickIncFocus() {
        robot.lookup("#focusSessionLabel").queryAs(Label.class).getParent().getParent().getChildrenUnmodifiable().stream()
                .filter(node -> node instanceof javafx.scene.control.Button btn && btn.getGraphic() instanceof FontIcon icon && "fltfal-add-20".equals(icon.getIconLiteral()))
                .findFirst().ifPresent(robot::clickOn);
        return this;
    }

    public SettingsOverlayViewPage clickDecFocus() {
        robot.lookup("#focusSessionLabel").queryAs(Label.class).getParent().getParent().getChildrenUnmodifiable().stream()
                .filter(node -> node instanceof javafx.scene.control.Button btn && btn.getGraphic() instanceof FontIcon icon && "fltfal-line-horizontal-1-20".equals(icon.getIconLiteral()))
                .findFirst().ifPresent(robot::clickOn);
        return this;
    }

    public String getShortBreakValue() {
        return robot.lookup("#shortBreakLabel").queryAs(Label.class).getText();
    }

    public SettingsOverlayViewPage clickIncShortBreak() {
        robot.lookup("#shortBreakLabel").queryAs(Label.class).getParent().getParent().getChildrenUnmodifiable().stream()
                .filter(node -> node instanceof javafx.scene.control.Button btn && btn.getGraphic() instanceof FontIcon icon && "fltfal-add-20".equals(icon.getIconLiteral()))
                .findFirst().ifPresent(robot::clickOn);
        return this;
    }

    public SettingsOverlayViewPage clickDecShortBreak() {
        robot.lookup("#shortBreakLabel").queryAs(Label.class).getParent().getParent().getChildrenUnmodifiable().stream()
                .filter(node -> node instanceof javafx.scene.control.Button btn && btn.getGraphic() instanceof FontIcon icon && "fltfal-line-horizontal-1-20".equals(icon.getIconLiteral()))
                .findFirst().ifPresent(robot::clickOn);
        return this;
    }

    public String getLongBreakValue() {
        return robot.lookup("#longBreakLabel").queryAs(Label.class).getText();
    }

    public SettingsOverlayViewPage clickIncLongBreak() {
        robot.lookup("#longBreakLabel").queryAs(Label.class).getParent().getParent().getChildrenUnmodifiable().stream()
                .filter(node -> node instanceof javafx.scene.control.Button btn && btn.getGraphic() instanceof FontIcon icon && "fltfal-add-20".equals(icon.getIconLiteral()))
                .findFirst().ifPresent(robot::clickOn);
        return this;
    }

    public SettingsOverlayViewPage clickDecLongBreak() {
        robot.lookup("#longBreakLabel").queryAs(Label.class).getParent().getParent().getChildrenUnmodifiable().stream()
                .filter(node -> node instanceof javafx.scene.control.Button btn && btn.getGraphic() instanceof FontIcon icon && "fltfal-line-horizontal-1-20".equals(icon.getIconLiteral()))
                .findFirst().ifPresent(robot::clickOn);
        return this;
    }

    public String getMaxSessionsValue() {
        return robot.lookup("#maxSessionsLabel").queryAs(Label.class).getText();
    }

    public SettingsOverlayViewPage clickIncMaxSessions() {
        robot.lookup("#maxSessionsLabel").queryAs(Label.class).getParent().getParent().getChildrenUnmodifiable().stream()
                .filter(node -> node instanceof javafx.scene.control.Button btn && btn.getGraphic() instanceof FontIcon icon && "fltfal-add-20".equals(icon.getIconLiteral()))
                .findFirst().ifPresent(robot::clickOn);
        return this;
    }

    public SettingsOverlayViewPage clickDecMaxSessions() {
        robot.lookup("#maxSessionsLabel").queryAs(Label.class).getParent().getParent().getChildrenUnmodifiable().stream()
                .filter(node -> node instanceof javafx.scene.control.Button btn && btn.getGraphic() instanceof FontIcon icon && "fltfal-line-horizontal-1-20".equals(icon.getIconLiteral()))
                .findFirst().ifPresent(robot::clickOn);
        return this;
    }

    public void clickClose() {
        robot.clickOn(node -> node instanceof FontIcon icon && "fltfal-dismiss-24".equals(icon.getIconLiteral()));
    }
}
