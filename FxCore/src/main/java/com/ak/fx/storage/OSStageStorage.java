package com.ak.fx.storage;

import javafx.stage.Stage;

public enum OSStageStorage {
  WINDOWS,
  MAC {
    @Override
    public Storage<Stage> newInstance(Class<?> clazz, String nodeName) {
      return new MacStageStorage(clazz, nodeName);
    }
  },
  UNIX;

  public Storage<Stage> newInstance(Class<?> clazz, String nodeName) {
    return new DefaultStageStorage(clazz, nodeName);
  }
}
