package com.ak.fx.storage;

import javax.annotation.Nonnull;

import javafx.stage.Stage;

final class DefaultStageStorage extends AbstractStageStorage {
  DefaultStageStorage(@Nonnull Class<?> c) {
    super(c);
  }

  @Override
  public void update(@Nonnull Stage stage) {
    stage.fullScreenProperty().addListener((observable, oldValue, newValue) -> saveFullScreenState(newValue));
    super.update(stage);
  }
}
