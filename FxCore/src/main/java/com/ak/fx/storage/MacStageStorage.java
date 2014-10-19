package com.ak.fx.storage;

import java.time.Duration;
import java.time.Instant;

import com.ak.storage.AbstractStorage;
import javafx.stage.Stage;

final class MacStageStorage extends AbstractStorage<Stage> {
  private final StageStorage stageStorage;
  private Instant fullScreenEventInstant = Instant.now();

  MacStageStorage(String filePrefix) {
    super(filePrefix);
    stageStorage = new StageStorage(filePrefix);
  }

  @Override
  public void save(Stage stage) {
    if (!Duration.between(fullScreenEventInstant, Instant.now()).minusSeconds(3).isNegative()) {
      stageStorage.saveFullScreenState(stage.isFullScreen());
    }
    stageStorage.save(stage);
  }

  @Override
  public void update(Stage stage) {
    stage.fullScreenProperty().addListener((observable, oldValue, newValue) -> {
      fullScreenEventInstant = Instant.now();
    });
    stageStorage.update(stage);
  }

  @Override
  public Stage get() {
    return stageStorage.get();
  }
}
