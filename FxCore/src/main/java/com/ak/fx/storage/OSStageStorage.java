package com.ak.fx.storage;

import com.ak.storage.Storage;
import javafx.stage.Stage;

public enum OSStageStorage {
  WINDOWS,
  MAC {
    @Override
    public Storage<Stage> newInstance() {
      return new MacStageStorage(OSStageStorage.class.getSimpleName());
    }
  },
  UNIX;

  public Storage<Stage> newInstance() {
    return new DefaultStageStorage(OSStageStorage.class.getSimpleName());
  }
}
