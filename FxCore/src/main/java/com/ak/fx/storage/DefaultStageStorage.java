package com.ak.fx.storage;

import javafx.stage.Stage;

final class DefaultStageStorage extends AbstractStageStorage {
  DefaultStageStorage(Class<?> c, String nodeName) {
    super(c, nodeName);
  }

  @Override
  public void update(Stage stage) {
    stage.fullScreenProperty().addListener((observable, oldValue, newValue) -> saveFullScreenState(newValue));
    super.update(stage);
  }
}
