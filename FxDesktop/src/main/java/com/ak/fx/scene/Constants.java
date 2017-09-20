package com.ak.fx.scene;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import static com.ak.fx.scene.GridCell.SMALL;

enum Constants {
  ;

  static final Font FONT_H1 = Font.font("Tahoma", FontWeight.BOLD, SMALL.getStep() / 2.5);
  static final Font FONT_H2 = Font.font("Tahoma", SMALL.getStep() / 3);
}
