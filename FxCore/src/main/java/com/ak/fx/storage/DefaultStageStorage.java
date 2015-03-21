package com.ak.fx.storage;

import javafx.stage.Stage;

final class DefaultStageStorage extends AbstractStageStorage {
  DefaultStageStorage(String filePrefix) {
    super(filePrefix);
  }

  @Override
  public void update(Stage stage) {
    stage.fullScreenProperty().addListener((observable, oldValue, newValue) -> saveFullScreenState(newValue));
    super.update(stage);
  }
}
