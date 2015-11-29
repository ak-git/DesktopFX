package com.ak.fx.storage;

import com.ak.storage.Storage;
import javafx.stage.Stage;

public enum OSStageStorage {
  WINDOWS,
  MAC {
    @Override
    public Storage<Stage> newInstance(Class<?> clazz) {
      return new MacStageStorage(clazz.getName());
    }
  },
  UNIX;

  public Storage<Stage> newInstance(Class<?> clazz) {
    return new DefaultStageStorage(clazz.getName());
  }
}
