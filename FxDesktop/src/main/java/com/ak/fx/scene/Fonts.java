package com.ak.fx.scene;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.fx.stage.ScreenResolutionMonitor;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import static com.ak.fx.scene.GridCell.SMALL;

enum Fonts {
  H1(FontWeight.BOLD, 2.5), H2(FontWeight.NORMAL, 3.0);

  private final transient ObjectProperty<Font> fontProperty;

  Fonts(@Nonnull FontWeight weight, @Nonnegative double divider) {
    fontProperty = new SimpleObjectProperty<>(newFont(weight, divider));
    ScreenResolutionMonitor.INSTANCE.dpi().addListener((observable, oldValue, newValue) -> fontProperty.set(newFont(weight, divider)));
  }

  ReadOnlyObjectProperty<Font> fontProperty() {
    return fontProperty;
  }

  private static Font newFont(@Nonnull FontWeight weight, @Nonnegative double divider) {
    return Font.font("Tahoma", weight, SMALL.getStep() / divider);
  }
}
