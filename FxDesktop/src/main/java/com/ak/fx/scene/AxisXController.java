package com.ak.fx.scene;

import java.util.function.DoubleConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.measure.quantity.Speed;

import com.ak.comm.converter.Variables;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

import static com.ak.fx.scene.GridCell.SMALL;

final class AxisXController {
  public enum ZoomX {
    Z_10(10), Z_25(25), Z_50(50);

    private final int mmPerSec;

    ZoomX(@Nonnegative int mmPerSec) {
      this.mmPerSec = mmPerSec;
    }

    ZoomX prev() {
      return values()[Math.max(0, ordinal() - 1)];
    }

    ZoomX next() {
      return values()[Math.min(values().length - 1, ordinal() + 1)];
    }

    @Override
    public String toString() {
      return Variables.toString(
          Quantities.getQuantity(mmPerSec, MetricPrefix.MILLI(Units.METRE).divide(Units.SECOND).asType(Speed.class))
      );
    }
  }

  private final IntegerProperty startProperty = new SimpleIntegerProperty();
  private final IntegerProperty lengthProperty = new SimpleIntegerProperty();
  private final ObjectProperty<ZoomX> zoomProperty = new SimpleObjectProperty<>(ZoomX.Z_25);
  @Nonnegative
  private double step;
  @Nonnegative
  private int decimateFactor = 1;

  AxisXController(@Nonnull Runnable onUpdate) {
    startProperty.addListener((observable, oldValue, newValue) -> onUpdate.run());
    lengthProperty.addListener((observable, oldValue, newValue) -> onUpdate.run());
  }

  @Override
  public String toString() {
    return String.format("Axis-X size = %d [%d - %d]; x-zoom = %d mm/s; decimate factor = %d",
        lengthProperty.get(), startProperty.get(), startProperty.get() + lengthProperty.get(),
        zoomProperty.get().mmPerSec, decimateFactor);
  }

  void setFrequency(@Nonnegative double frequency, @Nonnull DoubleConsumer xStep) {
    setStep(frequency, xStep);
    zoomProperty.addListener((observable, oldValue, newValue) -> setStep(frequency, xStep));
  }

  ReadOnlyObjectProperty<ZoomX> zoomProperty() {
    return zoomProperty;
  }

  void scroll(double deltaX) {
    setStart((int) Math.rint(startProperty.get() - deltaX * decimateFactor));
  }

  void zoom(double zoomFactor) {
    if (zoomFactor > 1) {
      zoomProperty.setValue(zoomProperty.get().next());
    }
    else if (zoomFactor < 1) {
      zoomProperty.setValue(zoomProperty.get().prev());
    }
  }

  void reset() {
    startProperty.set(0);
  }

  void checkLength(@Nonnegative int realDataLen) {
    startProperty.setValue(Math.max(0, startProperty.get() + realDataLen - lengthProperty.get()));
  }

  void preventCenter(@Nonnegative double width) {
    int prevChartCenter = startProperty.get() + lengthProperty.get() / 2;
    lengthProperty.setValue(width * decimateFactor / step);
    startProperty.setValue(Math.max(0, prevChartCenter - lengthProperty.get() / 2));
  }

  int getStart() {
    return startProperty.get();
  }

  int getEnd() {
    return startProperty.get() + lengthProperty.get();
  }

  int getDecimateFactor() {
    return decimateFactor;
  }

  private double getStep(@Nonnegative double frequency) {
    double pointsInSec = SMALL.getStep() * zoomProperty.get().mmPerSec / 10.0;
    decimateFactor = (int) Math.max(1, Math.rint(frequency / pointsInSec));
    double xStep = decimateFactor * pointsInSec / frequency;
    Logger.getLogger(getClass().getName()).log(Level.CONFIG,
        String.format("Frequency = %.0f Hz; x-zoom = %d mm/s; pixels per sec = %.1f; decimate factor = %d; x-step = %.1f px",
            frequency, zoomProperty.get().mmPerSec, pointsInSec, decimateFactor, xStep));
    return xStep;
  }

  private void setStart(int start) {
    startProperty.setValue(Math.max(0, start));
  }

  private void setStep(@Nonnegative double frequency, @Nonnull DoubleConsumer xStep) {
    step = getStep(frequency);
    xStep.accept(step);
  }
}
