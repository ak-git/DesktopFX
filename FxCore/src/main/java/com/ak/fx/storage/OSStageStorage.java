package com.ak.fx.storage;

import javax.annotation.Nonnull;

import javafx.stage.Stage;

public enum OSStageStorage {
  WINDOWS,
  MAC {
    @Override
    public Storage<Stage> newInstance(@Nonnull Class<?> clazz, @Nonnull String nodeName) {
      return new MacStageStorage(clazz, nodeName);
    }
  },
  UNIX;

  public Storage<Stage> newInstance(@Nonnull Class<?> clazz, @Nonnull String nodeName) {
    return new DefaultStageStorage(clazz, nodeName);
  }
}
