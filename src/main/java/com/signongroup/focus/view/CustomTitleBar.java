package com.signongroup.focus.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Custom window title bar for undecorated stages.
 * Provides window dragging and min/max/close controls.
 */
public class CustomTitleBar extends HBox {

    private double xOffset = 0;
    private double yOffset = 0;

    /**
     * Constructs a new CustomTitleBar.
     *
     * @param stage         The primary stage to control.
     * @param windowManager The window manager for mini-mode toggling.
     */
    public CustomTitleBar(Stage stage, WindowManager windowManager) {
        this.setAlignment(Pos.CENTER_LEFT);
        this.setPadding(new Insets(8, 12, 8, 16));
        this.setStyle("-fx-background-color: #0e0e0e;");
        this.setMinHeight(32);
        this.setMaxHeight(32);

        Label titleLabel = new Label("Focus Timer");
        titleLabel.setStyle("-fx-text-fill: -fx-primary; -fx-font-family: 'System', 'Inter', sans-serif; "
            + "-fx-font-weight: bold; -fx-font-size: 12px;");

        this.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        this.setOnMouseDragged(event -> {
            if (stage.isMaximized()) {
                return;
            }
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button minBtn = createIconButton("fltfmz-minimize-20");
        minBtn.setOnAction(e -> stage.setIconified(true));

        Button maxBtn = createIconButton("fltfmz-maximize-20");
        maxBtn.setOnAction(e -> {
            if (stage.getWidth() == 288) {
                windowManager.toggleMiniMode(false);
            } else {
                stage.setMaximized(!stage.isMaximized());
            }
        });

        Button closeBtn = createIconButton("fltfmz-dismiss-20");
        closeBtn.setOnAction(e -> Platform.exit());
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle("-fx-background-color: #e81123; -fx-cursor: hand; "
            + "-fx-padding: 4 8 4 8; -fx-background-radius: 4;"));
        closeBtn.setOnMouseExited(e -> closeBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; "
            + "-fx-padding: 4 8 4 8; -fx-background-radius: 4;"));

        this.getChildren().addAll(titleLabel, spacer, minBtn, maxBtn, closeBtn);
    }

    private Button createIconButton(String iconLiteral) {
        Button btn = new Button();
        btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 4 8 4 8; -fx-background-radius: 4;");
        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(14);
        icon.setStyle("-fx-icon-color: -fx-primary;");
        btn.setGraphic(icon);

        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #262626; -fx-cursor: hand; "
            + "-fx-padding: 4 8 4 8; -fx-background-radius: 4;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; "
            + "-fx-padding: 4 8 4 8; -fx-background-radius: 4;"));

        return btn;
    }
}
