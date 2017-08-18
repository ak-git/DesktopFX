package com.ak.fx.scene;

import javafx.scene.text.Font;

import static com.ak.fx.scene.GridCell.SMALL;

enum Constants {
  ;

  static final double LABEL_HEIGHT = SMALL.getStep() / 3;

  static final Font FONT = Font.font(Font.getDefault().getName(), LABEL_HEIGHT);
}
