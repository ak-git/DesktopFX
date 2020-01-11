package com.ak.fx.storage;

import javax.annotation.Nonnull;

import javafx.stage.Stage;

public enum OSStageStorage {
  WINDOWS,
  MAC {
    @Override
    public Storage<Stage> newInstance(@Nonnull Class<?> clazz) {
      return new MacStageStorage(clazz);
    }
  },
  UNIX;

  public Storage<Stage> newInstance(@Nonnull Class<?> clazz) {
    return new DefaultStageStorage(clazz);
  }
}
