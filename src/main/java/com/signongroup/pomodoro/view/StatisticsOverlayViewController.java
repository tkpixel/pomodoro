package com.signongroup.pomodoro.view;

import com.signongroup.pomodoro.viewmodel.StatisticsViewModel;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

@Prototype
public class StatisticsOverlayViewController implements Initializable {

  @FXML private StackPane overlayRoot;

  @FXML private Region backgroundRegion;

  @FXML private VBox bottomSheet;

  @FXML private Label clearedTodayLabel;
  @FXML private Label weekLabel;
  @FXML private Label sprintClearedLabel;
  @FXML private Label sprintPlannedLabel;
  @FXML private Label yearLabel;

  @FXML private Label sprintClearedTitleLabel;

  @FXML private Label sprintPlannedTitleLabel;

  private final StatisticsViewModel statisticsViewModel;

  @Inject
  public StatisticsOverlayViewController(StatisticsViewModel statisticsViewModel) {
    this.statisticsViewModel = statisticsViewModel;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    // Bind UI to ViewModel
    clearedTodayLabel
        .textProperty()
        .bind(statisticsViewModel.clearedTodayProperty().asString("%02d"));
    weekLabel.textProperty().bind(statisticsViewModel.weekProperty().asString("%02d"));
    sprintClearedLabel.textProperty().bind(statisticsViewModel.sprintClearedProperty().asString());
    sprintPlannedLabel
        .textProperty()
        .bind(Bindings.concat("/ ", statisticsViewModel.sprintPlannedProperty()));
    yearLabel.textProperty().bind(statisticsViewModel.yearProperty().asString());

    if (sprintClearedTitleLabel != null) {
      sprintClearedTitleLabel.textProperty().bind(statisticsViewModel.sprintClearedTitleProperty());
    }
    if (sprintPlannedTitleLabel != null) {
      sprintPlannedTitleLabel.textProperty().bind(statisticsViewModel.sprintPlannedTitleProperty());
    }

    // Ensure starting state is hidden
    overlayRoot.setVisible(false);
    bottomSheet.setTranslateY(1000);
  }

  public void open() {
    overlayRoot.setVisible(true);
    TranslateTransition openTransition = new TranslateTransition(Duration.millis(300), bottomSheet);
    openTransition.setToY(0);
    openTransition.setInterpolator(Interpolator.EASE_OUT);
    openTransition.play();
  }

  @FXML
  public void handleClose() {
    TranslateTransition closeTransition =
        new TranslateTransition(Duration.millis(300), bottomSheet);
    closeTransition.setToY(1000);
    closeTransition.setInterpolator(Interpolator.EASE_IN);
    closeTransition.setOnFinished(e -> overlayRoot.setVisible(false));
    closeTransition.play();
  }
}
