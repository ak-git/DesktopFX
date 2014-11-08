package com.ak.fx.storage;

import java.time.Duration;
import java.time.Instant;

import javafx.stage.Stage;

final class MacStageStorage extends StageStorage {
  private Instant fullScreenEventInstant = Instant.now();

  MacStageStorage(String filePrefix) {
    super(filePrefix);
  }

  @Override
  public void save(Stage stage) {
    if (!Duration.between(fullScreenEventInstant, Instant.now()).minusSeconds(3).isNegative()) {
      saveFullScreenState(stage.isFullScreen());
    }
    super.save(stage);
  }

  @Override
  public void update(Stage stage) {
    stage.fullScreenProperty().addListener((observable, oldValue, newValue) -> fullScreenEventInstant = Instant.now());
    super.update(stage);
  }
}
