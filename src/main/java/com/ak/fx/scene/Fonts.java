package com.ak.fx.scene;

import com.ak.fx.stage.ScreenResolutionMonitor;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.function.Supplier;

import static com.ak.fx.scene.GridCell.SMALL;

public enum Fonts {
  LOGO(java.awt.Font.MONOSPACED, FontWeight.BOLD, 1.0),
  LOGO_SMALL(java.awt.Font.MONOSPACED, FontWeight.BOLD, 9.0),
  H1(Constants.TAHOMA, FontWeight.BOLD, 2.5),
  H2(Constants.TAHOMA, FontWeight.NORMAL, 3.0);

  public static final Paint COLOR = new Color(225.0 / 255.0, 130.0 / 255.0, 110.0 / 255.0, 1.0);

  private final transient ObjectProperty<Font> fontProperty;
  private final transient ChangeListener<Number> changeListener;

  Fonts(@Nonnull String family, @Nonnull FontWeight weight, @Nonnegative double divider) {
    fontProperty = new SimpleObjectProperty<>(newFont(family, weight, divider));
    changeListener = (observable, oldValue, newValue) -> fontProperty.set(newFont(family, weight, divider));
  }

  public ReadOnlyObjectProperty<Font> fontProperty(@Nonnull Supplier<Scene> stageSupplier) {
    ObservableValue<Number> dpi = ScreenResolutionMonitor.dpi(stageSupplier);
    dpi.removeListener(changeListener);
    dpi.addListener(changeListener);
    return fontProperty;
  }

  private static Font newFont(@Nonnull String family, @Nonnull FontWeight weight, @Nonnegative double divider) {
    return Font.font(family, weight, SMALL.getStep() / divider);
  }

  private enum Constants {
    ;
    private static final String TAHOMA = "Tahoma";
  }
}
