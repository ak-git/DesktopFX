package com.ak.fx.util;

import javafx.application.Platform;

import java.util.Objects;

public enum FxUtils {
  ;

  public static void invokeInFx(Runnable runnable) {
    if (Platform.isFxApplicationThread()) {
      runnable.run();
    }
    else {
      Platform.runLater(Objects.requireNonNull(runnable));
    }
  }
}
