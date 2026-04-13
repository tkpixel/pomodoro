package com.signongroup.pomodoro.view;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Arc;
import org.kordamp.ikonli.javafx.FontIcon;
import org.testfx.api.FxRobot;

public class MainViewPage {

    private final FxRobot robot;

    public MainViewPage(FxRobot robot) {
        this.robot = robot;
    }

    public Arc getBaseArc() {
        return robot.lookup("#baseArc").queryAs(Arc.class);
    }

    public Arc getProgressArc() {
        return robot.lookup("#progressArc").queryAs(Arc.class);
    }

    public String getTimerText() {
        return robot.lookup("#timerLabel").queryAs(Label.class).getText();
    }

    public String getSessionText() {
        return robot.lookup("#sessionLabel").queryAs(Label.class).getText();
    }

    public String getClearedTodayText() {
        return robot.lookup("#clearedTodayLabel").queryAs(Label.class).getText();
    }

    public HBox getBreakCardContainer() {
        return robot.lookup("#breakCardContainer").queryAs(HBox.class);
    }

    public StackPane getBreakCardIconContainer() {
        return robot.lookup("#breakCardIconContainer").queryAs(StackPane.class);
    }

    public FontIcon getBreakIcon() {
        return robot.lookup("#breakIcon").queryAs(FontIcon.class);
    }

    public String getBreakTitleText() {
        return robot.lookup("#breakTitleLabel").queryAs(Label.class).getText();
    }

    public String getNextBreakText() {
        return robot.lookup("#nextBreakLabel").queryAs(Label.class).getText();
    }

    public Region getBreakProgressRegion() {
        return robot.lookup("#breakProgressRegion").queryAs(Region.class);
    }

    public MainViewPage clickSkip() {
        robot.clickOn("#skipButton");
        return this;
    }

    public FontIcon getPlayIcon() {
        return robot.lookup("#playIcon").queryAs(FontIcon.class);
    }

    public MainViewPage clickPlayPause() {
        // play button doesn't have an fx:id, but its graphic does (#playIcon)
        robot.clickOn("#playIcon");
        return this;
    }

    public void clickOpenSettingsMenu() {
        robot.clickOn(node -> node instanceof FontIcon icon && "fltral-line-horizontal-3-20".equals(icon.getIconLiteral()));
    }
}
