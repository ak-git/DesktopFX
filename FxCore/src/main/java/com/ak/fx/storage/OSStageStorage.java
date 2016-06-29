package com.ak.fx.storage;

import javax.annotation.Nonnull;

import com.ak.storage.Storage;
import javafx.stage.Stage;

public enum OSStageStorage {
  WINDOWS,
  MAC {
    @Nonnull
    @Override
    public Storage<Stage> newInstance(@Nonnull Class<?> clazz) {
      return new MacStageStorage(clazz.getName());
    }
  },
  UNIX;

  @Nonnull
  public Storage<Stage> newInstance(@Nonnull Class<?> clazz) {
    return new DefaultStageStorage(clazz.getName());
  }
}
