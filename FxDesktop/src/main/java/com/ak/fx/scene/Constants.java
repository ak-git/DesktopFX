package com.ak.fx.scene;

import javafx.application.Platform;
import javafx.scene.text.Font;

import static com.ak.fx.scene.GridCell.SMALL;

enum Constants {
  ;

  static final double LABEL_HEIGHT = SMALL.getStep() / 3;

  static final Font FONT = Font.font(Font.getDefault().getName(), LABEL_HEIGHT);

  static void invokeInFx(Runnable runnable) {
    if (Platform.isFxApplicationThread()) {
      runnable.run();
    }
    else {
      Platform.runLater(runnable);
    }
  }
}
