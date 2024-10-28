package com.ak.fx.scene;

import com.ak.fx.stage.ScreenResolutionMonitor;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import javax.annotation.Nonnegative;
import java.util.function.Supplier;

import static com.ak.fx.scene.GridCell.SMALL;
import static java.awt.Font.MONOSPACED;

public enum Fonts {
  LOGO(MONOSPACED, FontWeight.BOLD, 1.0),
  LOGO_SMALL(MONOSPACED, FontWeight.BOLD, 9.0),
  H1(Constants.SYSTEM, FontWeight.BOLD, 2.5),
  H2(Constants.SYSTEM, FontWeight.NORMAL, 3.0);

  private final transient ObjectProperty<Font> fontProperty;
  private final transient ChangeListener<Number> changeListener;

  Fonts(String family, FontWeight weight, @Nonnegative double divider) {
    fontProperty = new SimpleObjectProperty<>(newFont(family, weight, divider));
    changeListener = (_, _, _) -> fontProperty.set(newFont(family, weight, divider));
  }

  public ReadOnlyObjectProperty<Font> fontProperty(Supplier<Scene> stageSupplier) {
    ObservableValue<Number> dpi = ScreenResolutionMonitor.dpi(stageSupplier);
    dpi.removeListener(changeListener);
    dpi.addListener(changeListener);
    return fontProperty;
  }

  private static Font newFont(String family, FontWeight weight, @Nonnegative double divider) {
    return Font.font(family, weight, SMALL.getStep() / divider);
  }

  private enum Constants {
    ;
    private static final String TAHOMA = "Tahoma";
    private static final String SYSTEM = Font.getFontNames().contains(TAHOMA) ? TAHOMA : Font.getDefault().getName();
  }
}
