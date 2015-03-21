package com.ak.fx.storage;

import com.ak.storage.Storage;
import javafx.stage.Stage;

public enum OSStageStorage {
  WINDOWS,
  MAC {
    @Override
    public Storage<Stage> newInstance(String filePrefix) {
      return new MacStageStorage(filePrefix);
    }
  },
  UNIX;

  public Storage<Stage> newInstance(String filePrefix) {
    return new DefaultStageStorage(filePrefix);
  }
}
