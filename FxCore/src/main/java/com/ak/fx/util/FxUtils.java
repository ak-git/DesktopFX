package com.ak.fx.util;

import javax.annotation.Nonnull;

import javafx.application.Platform;

public enum FxUtils {
  ;

  public static final int WIDTH_MIN = 1024;
  public static final int HEIGHT_MIN = 768;

  public static void invokeInFx(@Nonnull Runnable runnable) {
    if (Platform.isFxApplicationThread()) {
      runnable.run();
    }
    else {
      Platform.runLater(runnable);
    }
  }
}
